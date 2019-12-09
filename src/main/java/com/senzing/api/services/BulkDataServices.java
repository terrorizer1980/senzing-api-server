package com.senzing.api.services;

import com.senzing.api.model.*;
import com.senzing.g2.engine.G2Engine;
import com.senzing.io.IOUtilities;
import com.senzing.io.RecordReader;
import com.senzing.io.TemporaryDataCache;
import com.senzing.util.AsyncWorkerPool;
import com.senzing.util.JsonUtils;
import com.senzing.util.LoggingUtilities;
import com.senzing.util.Timers;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.*;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.senzing.api.model.SzHttpMethod.POST;
import static com.senzing.api.services.ServicesUtil.*;
import static com.senzing.text.TextUtilities.*;
import static com.senzing.util.AsyncWorkerPool.*;
import static com.senzing.api.model.SzBulkLoadStatus.*;

/**
 * Bulk data REST services.
 */
@Path("/bulk-data")
@Produces("application/json; charset=UTF-8")
public class BulkDataServices {
  /**
   * The file date pattern.
   */
  private static final String FILE_DATE_PATTERN = "yyyyMMdd_HHmmssX";

  /**
   * The time zone used for the time component of the build number.
   */
  private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

  /**
   * The formatter for the file date.
   */
  private static final DateTimeFormatter FILE_DATE_FORMATTER
      = DateTimeFormatter.ofPattern(FILE_DATE_PATTERN);

  /**
   * Analyzes the bulk data records.
   */
  @POST
  @Path("/analyze")
  public SzBulkDataAnalysisResponse analyzeBulkRecordsViaForm(
      @HeaderParam("Content-Type") MediaType mediaType,
      @FormDataParam("data") InputStream dataInputStream,
      @Context UriInfo uriInfo)
  {
    try {
      return this.analyzeBulkRecords(mediaType, dataInputStream, uriInfo);
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Analyzes the bulk data records.
   */
  @POST
  @Path("/analyze")
  @Consumes({ MediaType.APPLICATION_JSON,
              MediaType.TEXT_PLAIN,
              "text/csv",
              "application/x-jsonlines"})
  public SzBulkDataAnalysisResponse analyzeBulkRecordsDirect(
      @HeaderParam("Content-Type") MediaType mediaType,
      InputStream dataInputStream,
      @Context UriInfo uriInfo)
  {
    try {
      return this.analyzeBulkRecords(mediaType, dataInputStream, uriInfo);
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Formats load ID using the specified data cache
   */
  private static String formatLoadId(TemporaryDataCache         dataCache,
                                     FormDataContentDisposition fileMetaData)
  {
    String fileKey = (fileMetaData != null) ? fileMetaData.getName() : null;
    if (fileKey == null) {
      try (InputStream is = dataCache.getInputStream();)
      {
        byte[]        bytes     = new byte[1024];
        MessageDigest md5       = MessageDigest.getInstance("MD5");
        int           readCount = is.read(bytes);
        md5.update(bytes, 0, readCount);
        byte[] hash = md5.digest();
        fileKey = Base64.getEncoder().encodeToString(hash);

      } catch (Exception e) {
        fileKey = randomPrintableText(30);
      }
    }
    Date fileDate = (fileMetaData != null)
        ? fileMetaData.getModificationDate()
        : null;

    if (fileDate == null && fileMetaData != null) {
      fileDate = fileMetaData.getCreationDate();
    }

    ZonedDateTime fileDateTime = (fileDate == null)
        ? null : ZonedDateTime.ofInstant(fileDate.toInstant(), UTC_ZONE);
    ZonedDateTime now = ZonedDateTime.now(UTC_ZONE);

    String fileDateText = (fileDate == null)
        ? "?" : FILE_DATE_FORMATTER.format(fileDate.toInstant());
    String nowText = (now == null) ? "?" : FILE_DATE_FORMATTER.format(now);

    return fileKey + "_" + fileDateText + "_" + nowText;
  }

  /**
   * Analyzes the bulk data records.
   */
  @POST
  @Path("/load")
  public SzBulkLoadResponse loadBulkRecordsViaForm(
      @QueryParam("dataSource") String dataSource,
      @QueryParam("loadId") String loadId,
      @DefaultValue("-1") @QueryParam("maxFailures") int maxFailures,
      @HeaderParam("Content-Type") MediaType mediaType,
      @FormDataParam("data") InputStream dataInputStream,
      @FormDataParam("data") FormDataContentDisposition fileMetaData,
      @Context UriInfo uriInfo)
  {
    try {
      return this.loadBulkRecords(dataSource,
                                  loadId,
                                  maxFailures,
                                  mediaType,
                                  dataInputStream,
                                  fileMetaData,
                                  uriInfo);

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Analyzes the bulk data records.
   */
  @POST
  @Path("/load")
  @Consumes({ MediaType.APPLICATION_JSON,
      MediaType.TEXT_PLAIN,
      "text/csv",
      "application/x-jsonlines"})
  public SzBulkLoadResponse loadBulkRecordsDirect(
      @QueryParam("dataSource") String dataSource,
      @QueryParam("loadId") String loadId,
      @DefaultValue("-1") @QueryParam("maxFailures") int maxFailures,
      @HeaderParam("Content-Type") MediaType mediaType,
      InputStream dataInputStream,
      @Context UriInfo uriInfo)
  {
    try {
      return this.loadBulkRecords(dataSource,
                                  loadId,
                                  maxFailures,
                                  mediaType,
                                  dataInputStream,
                                  null,
                                  uriInfo);

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Analyzes the bulk data and returns information about it.
   */
  private SzBulkDataAnalysisResponse analyzeBulkRecords(
      MediaType                   mediaType,
      InputStream                 dataInputStream,
      UriInfo                     uriInfo)
  {
    System.out.println("MEDIA TYPE: " + mediaType);
    SzBulkDataAnalysis dataAnalysis = new SzBulkDataAnalysis();
    Timers timers = newTimers();
    try {
      BulkDataSet bulkDataSet = new BulkDataSet(mediaType, dataInputStream);
      TemporaryDataCache dataCache = bulkDataSet.dataCache;

      // if charset is unknown then try to detect
      String charset = bulkDataSet.characterEncoding;
      dataAnalysis.setCharacterEncoding(charset);

      // check if we need to auto-detect the media type
      try (InputStream        is  = dataCache.getInputStream(true);
           InputStreamReader  isr = new InputStreamReader(is, charset);
           BufferedReader     br  = new BufferedReader(isr))
      {
        // if format is null then RecordReader will auto-detect
        RecordReader recordReader = new RecordReader(bulkDataSet.format, br);
        bulkDataSet.format = recordReader.getFormat();
        System.out.println("DETECTED RECORD FORMAT: " + bulkDataSet.format);
        dataAnalysis.setMediaType(bulkDataSet.format.getMediaType());

        for (JsonObject record = recordReader.readRecord();
             (record != null);
             record = recordReader.readRecord())
        {
          String dataSrc  = JsonUtils.getString(record, "DATA_SOURCE");
          String recordId = JsonUtils.getString(record, "RECORD_ID");
          dataAnalysis.trackRecord(dataSrc, recordId);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
      throw newInternalServerErrorException(POST, uriInfo, timers, e);
    }

    return new SzBulkDataAnalysisResponse(POST,
                                          200,
                                          uriInfo,
                                          timers,
                                          dataAnalysis);
  }

  /**
   * Analyzes the bulk data and returns information about it.
   */
  private SzBulkLoadResponse loadBulkRecords(
      String                      dataSource,
      String                      explicitLoadId,
      int                         maxFailures,
      MediaType                   mediaType,
      InputStream                 dataInputStream,
      FormDataContentDisposition  fileMetaData,
      UriInfo                     uriInfo)
  {
    SzBulkLoadResult bulkLoadResult = new SzBulkLoadResult();

    if (dataSource != null) {
      dataSource = dataSource.trim().toUpperCase();
    }

    Timers timers = newTimers();
    SzApiProvider provider = SzApiProvider.Factory.getProvider();
    ensureLoadingIsAllowed(provider, POST, uriInfo, timers);
    Map<String, String> dataSourceMap = new HashMap<>();
    MultivaluedMap<String,String> params = uriInfo.getQueryParameters(true);
    params.entrySet().forEach(e -> {
      String key = e.getKey();
      if (key == null) return; // skip this one
      key = key.trim().toUpperCase();
      if (!key.toUpperCase().startsWith("DATASOURCE_")
          || key.equals("DATASOURCE_"))
      {
        // skip this one
        return;
      }
      String origDataSource = key.substring("DATASOURCE_".length());
      List<String> values = e.getValue();
      String newDataSource = values.get(0);
      if (newDataSource == null) return; // skip this one
      newDataSource = newDataSource.trim().toUpperCase();
      dataSourceMap.put(origDataSource, newDataSource);
    });
    dataSourceMap.put(null, dataSource);
    dataSourceMap.put("", dataSource);

    try {
      BulkDataSet bulkDataSet = new BulkDataSet(mediaType, dataInputStream);

      TemporaryDataCache dataCache = bulkDataSet.dataCache;

      String charset = bulkDataSet.characterEncoding;

      String loadId = (explicitLoadId == null)
          ? formatLoadId(dataCache, fileMetaData) : explicitLoadId;

      int concurrency = provider.getConcurrency();
      AsyncWorkerPool<EngineResult> asyncPool
          = new AsyncWorkerPool<>(loadId, concurrency);

      List<Timers> timerPool = new ArrayList<>(concurrency);
      for (int index = 0; index < concurrency; index++) {
        timerPool.add(new Timers());
      }

      // check if we need to auto-detect the media type
      try (InputStream        is  = dataCache.getInputStream(true);
           InputStreamReader  isr = new InputStreamReader(is, charset);
           BufferedReader     br  = new BufferedReader(isr))
      {
        // if format is null then RecordReader will auto-detect
        RecordReader recordReader = new RecordReader(bulkDataSet.format,
                                                     br,
                                                     dataSourceMap,
                                                     loadId);
        bulkDataSet.format = recordReader.getFormat();
        bulkLoadResult.setCharacterEncoding(charset);
        bulkLoadResult.setMediaType(bulkDataSet.format.getMediaType());

        // loop through the records and handle each record
        for (JsonObject record = recordReader.readRecord();
             (record != null);
             record = recordReader.readRecord()) {
          // check if we have a data source
          String resolvedDS = JsonUtils.getString(record, "DATA_SOURCE");
          if (resolvedDS == null || resolvedDS.trim().length() == 0) {
            bulkLoadResult.trackIncompleteRecord();
          } else {
            Timers subTimers  = timerPool.remove(0);
            AsyncResult<EngineResult> asyncResult = null;
            try {
              asyncResult = this.asyncProcessRecord(asyncPool,
                                                    provider,
                                                    subTimers,
                                                    record,
                                                    loadId);
            } finally {
              this.trackLoadResult(asyncResult, bulkLoadResult);
              timerPool.add(subTimers);
            }
          }

          // count the number of failures
          int failedCount = bulkLoadResult.getFailedRecordCount()
                          + bulkLoadResult.getIncompleteRecordCount();

          if (maxFailures >= 0 && failedCount > maxFailures) {
            bulkLoadResult.setStatus(ABORTED);
            break;
          }
        }

        // close out any in-flight loads from the asynchronous pool
        List<AsyncResult<EngineResult>> results = asyncPool.close();
        for (AsyncResult<EngineResult> asyncResult : results) {
          this.trackLoadResult(asyncResult, bulkLoadResult);
        }

        // merge the timers
        for (Timers subTimer: timerPool) {
          timers.mergeWith(subTimer);
        }

        bulkLoadResult.setStatus(COMPLETED);

      } finally {
        dataCache.delete();
      }

    } catch (IOException e) {
      if (!LoggingUtilities.isLastLoggedException(e)) {
        e.printStackTrace();
      }
      throw newInternalServerErrorException(POST, uriInfo, timers, e);
    }

    return new SzBulkLoadResponse(POST,
                                  200,
                                  uriInfo,
                                  timers,
                                  bulkLoadResult);
  }

  /**
   * Asynchronously process a record using the specified {@link SzApiProvider}
   * and {@link AsyncWorkerPool}.  The returned {@link AsyncResult} is from
   * a previously executed task on the same thread or <tt>null</tt> if the
   * worker thread employed has not previously executed a task.
   */
  private AsyncResult<EngineResult> asyncProcessRecord(
      AsyncWorkerPool<EngineResult> asyncPool,
      SzApiProvider                 provider,
      Timers                        timers,
      JsonObject                    record,
      String                        loadId)
  {
    String dataSource = JsonUtils.getString(record, "DATA_SOURCE");
    String recordId   = JsonUtils.getString(record, "RECORD_ID");
    String recordJSON = JsonUtils.toJsonText(record);

    G2Engine engineApi = provider.getEngineApi();
    return asyncPool.execute(() -> {
      try {
        // otherwise try to load the record
        enteringQueue(timers);
        return provider.executeInThread(() -> {
          exitingQueue(timers);
          int returnCode;
          if (recordId == null) {
            callingNativeAPI(timers, "engine", "addRecord");
            returnCode = engineApi.addRecord(dataSource,
                                             recordId,
                                             recordJSON,
                                             loadId);
            calledNativeAPI(timers, "engine", "addRecord");

          } else {
            callingNativeAPI(timers, "engine",
                             "addRecordWithReturnedRecordID");
            StringBuffer sb = new StringBuffer();
            returnCode = engineApi.addRecordWithReturnedRecordID(
                dataSource, sb, recordJSON, loadId);
            calledNativeAPI(timers, "engine",
                            "addRecordWithReturnedRecordID");
          }
          return new EngineResult(
              dataSource, timers, returnCode, engineApi);
        });

      } catch (Exception e) {
        throw new Exception(dataSource, e);
      }
    });
  }

  /**
   * Tracks the asynchronous record load result in the {@link SzBulkLoadResult}.
   */
  private void trackLoadResult(AsyncResult<EngineResult> asyncResult,
                               SzBulkLoadResult          bulkLoadResult)
  {
    // check the result
    if (asyncResult != null) {
      try {
        // get the value from the async result
        EngineResult engineResult = asyncResult.getValue();

        // check if the add failed or succeeded
        if (engineResult.isFailed()) {
          // adding the record failed, record the failure
          bulkLoadResult.trackFailedRecord(
              engineResult.dataSource,
              engineResult.errorCode,
              engineResult.errorMsg);
        } else {
          // adding the record succeeded, record the loaded record
          bulkLoadResult.trackLoadedRecord(engineResult.dataSource);
        }
      } catch (Exception e) {
        // an exception was thrown in trying to get the result
        String failedDataSource = e.getMessage();
        Throwable cause = e.getCause();
        bulkLoadResult.trackFailedRecord(
            failedDataSource, new SzError(cause.getMessage()));
      }
    }
  }

  /**
   * Encapsulates a bulk data set.
   */
  private static class BulkDataSet {
    private String characterEncoding;
    private String mediaType = null;
    private RecordReader.Format format;
    private TemporaryDataCache dataCache;

    public BulkDataSet(MediaType mediaType, InputStream inputStream)
        throws IOException
    {
      this.characterEncoding = mediaType.getParameters().get("charset");
      String baseMediaType = mediaType.getType() + "/" + mediaType.getSubtype();
      if (baseMediaType != null) baseMediaType = baseMediaType.toLowerCase();
      switch (baseMediaType) {
        case "multipart/form-data":
          this.characterEncoding = null;
        default:
          this.format = RecordReader.Format.fromMediaType(baseMediaType);
      }

      if (this.format != null) {
        this.mediaType = this.format.getMediaType();
      }

      try {
        this.dataCache = new TemporaryDataCache(inputStream);

        // if charset is unknown then try to detect
        if (this.characterEncoding == null) {
          try (InputStream is = this.dataCache.getInputStream()) {
            this.characterEncoding = IOUtilities.detectCharacterEncoding(is);
          }
          if (this.characterEncoding == null) this.characterEncoding = "UTF-8";
        }

      } catch (IOException e) {
        e.printStackTrace();
        LoggingUtilities.setLastLoggedAndThrow(e);
      }
    }
  }

  /**
   * The result from the engine.
   */
  public static class EngineResult {
    private int     returnCode  = 0;
    private String  dataSource  = null;
    private String  errorCode   = null;
    private String  errorMsg    = null;
    private Timers  timers      = null;
    private EngineResult(String   dataSource,
                         Timers   timers,
                         int      returnCode,
                         G2Engine engine)
    {
      this.dataSource = dataSource;
      this.returnCode = returnCode;
      this.timers     = timers;
      if (this.returnCode != 0) {
        this.errorCode  = "" + engine.getLastExceptionCode();
        this.errorMsg   = engine.getLastException();
      }
    }
    private boolean isFailed() {
      return (this.returnCode != 0);
    }
  }

}
