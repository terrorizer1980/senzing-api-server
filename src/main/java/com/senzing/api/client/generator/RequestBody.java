package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.util.JsonUtils;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Describes the request body for a {@link RestOperation}.
 */
public class RequestBody implements SpecConstruct {
  /**
   * The description for the request body.
   */
  private String description;

  /**
   * Flag indicating if the request body is required.
   */
  private boolean required;

  /**
   * The {@link ApiDataType} for an object request body that is encoded as JSON.
   */
  private ApiDataType bodyType;

  /**
   * Whether or not typed blob content is allowed.
   */
  private Set<MediaType> blobMediaTypes;

  /**
   * Default constructor.
   */
  public RequestBody() {
    this.description    = null;
    this.required       = false;
    this.bodyType       = null;
    this.blobMediaTypes = null;
  }

  /**
   * Gets the description for the body content.
   *
   * @return The description for the body content.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Sets the description for the body content.
   *
   * @param description The description for the body content.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Checks whether or not the request body is required.
   *
   * @return <tt>true</tt> if the body content is required, otherwise
   *         <tt>false</tt>.
   */
  public boolean isRequired() {
    return this.required;
  }

  /**
   * Sets the whether or not the request body is required.
   *
   * @param required <tt>true</tt> if the request body is required, otherwise
   *                 <tt>false</tt>.
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * Gets the {@link ApiDataType} describing the type of data for the request
   * body.
   *
   * @return The {@link ApiDataType} describing the type of data for the request
   *         body.
   */
  public ApiDataType getBodyType() {
    return this.bodyType;
  }

  /**
   * Sets the {@link ApiDataType} describing the type of data for the request
   * body.
   *
   * @param bodyType The {@link ApiDataType} describing the type of data for
   *                    the request body.
   */
  public void setBodyType(ApiDataType bodyType) {
    this.bodyType = bodyType;
  }

  /**
   * Gets the <b>unmodifiable</b> {@link Set} of blob media types associated
   * with the request body.  This returns <tt>null</tt> if there are no blob
   * media types for the operation.
   *
   * @return The <b>unmodifiable</b> {@link Set} of blob media types associated
   *         with the request body, or <tt>null</tt> if there are no blob
   *         media types for the operation.
   */
  public Set<MediaType> getBlobMediaTypes() {
    if (this.blobMediaTypes == null) return null;
    return Collections.unmodifiableSet(this.blobMediaTypes);
  }

  /**
   * Sets the blob media types associated with the request body using the types
   * contained in the specified collection.  Set this to <tt>null</tt> if there
   * are no blob media types for the operation.
   *
   * @param mediaTypes The {@link Collection} of blob media types to associate
   *                   with the request body, or <tt>null</tt> if there are no
   *                   blob media types for the operation.
   */
  public void setBlobMediaTypes(Collection<MediaType> mediaTypes) {
    if (mediaTypes == null) {
      this.blobMediaTypes = null;
    } else {
      if (this.blobMediaTypes == null) {
        this.blobMediaTypes = new LinkedHashSet<>();
      } else {
        this.blobMediaTypes.clear();
      }
      this.blobMediaTypes.addAll(mediaTypes);
    }
  }

  /**
   * Adds the specified {@link MediaType} to the {@link Set} of blob media
   * types associated with this request body.
   *
   * @param mediaType The {@link MediaType} to add to the {@link Set} of
   *                  blob media types associated with this request body.
   */
  public void addMediaType(MediaType mediaType) {
    if (this.blobMediaTypes == null) {
      this.blobMediaTypes = new LinkedHashSet<>();
    }
    this.blobMediaTypes.add(mediaType);
  }

  /**
   * Removes the specified blob media type from the {@link Set} of blob
   * media types associated with the request body.  If the specified blob
   * media type is not present then this method has no effect.
   *
   * @param mediaType The {@link MediaType} to remove.
   */
  public void removeMediaType(MediaType mediaType) {
    if (this.blobMediaTypes == null) return;
    this.blobMediaTypes.remove(mediaType);
  }

  /**
   * Removes all the blob media types associated with the request body.
   *
   */
  public void clearMediaTypes() {
    if (this.blobMediaTypes == null) return;
    this.blobMediaTypes.clear();
  }

  /**
   * Overridden to return <tt>true</tt> if and only if the specified parameter
   * is a non-null reference to an object of the same class with equivalent
   * properties.
   *
   * @param object The object to compare with.
   *
   * @return <tt>true</tt> if this object is equivalent to the specified
   *         parameter, otherwise <tt>false</tt>.
   */
  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    RequestBody that = (RequestBody) object;
    return isRequired() == that.isRequired() &&
        Objects.equals(this.getDescription(), that.getDescription()) &&
        Objects.equals(this.getBodyType(),
                       that.getBodyType());
  }

  /**
   * Overridden to return a hash code that is consistent with the {@link
   * #equals(Object)} implementation.
   *
   * @return The hash code for this instance.
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.getDescription(),
                        this.isRequired(),
                        this.getBodyType());
  }

  /**
   * Builds the JSON representation for this object.
   *
   * @param builder The {@link JsonObjectBuilder} for creating the JSON
   *                representation of this object.
   */
  public void buildJson(JsonObjectBuilder builder) {
    // add the description
    String desc = this.getDescription();
    if (desc != null && desc.trim().length() > 0) {
      builder.add("description", desc);
    }

    // add the required flag
    builder.add("required", this.isRequired());

    // create the content builder
    JsonObjectBuilder contentBuilder = Json.createObjectBuilder();
    boolean hasContent = false;

    // add the content type
    if (this.getBodyType() != null) {
      ApiDataType objContentType = this.getBodyType();
      JsonObjectBuilder schemaBuilder = Json.createObjectBuilder();
      objContentType.buildJson(schemaBuilder);

      JsonObjectBuilder mimeBuilder = Json.createObjectBuilder();
      mimeBuilder.add("schema", schemaBuilder);

      contentBuilder.add("application/json; charset=UTF-8", mimeBuilder);
      contentBuilder.add("application/json", mimeBuilder);
      hasContent = true;
    } else if (this.getBlobMediaTypes() != null) {
      JsonObjectBuilder schemaBuilder = Json.createObjectBuilder();
      schemaBuilder.add("type", "string");
      for (MediaType mediaType: this.getBlobMediaTypes()) {
        contentBuilder.add(mediaType.toString(), schemaBuilder);
      }
    }

    // add the content section if there is content
    if (hasContent) builder.add("content", contentBuilder);
  }

  /**
   * Return a JSON {@link String} describing this instance.
   *
   * @return A JSON {@link String} describing this instance.
   */
  public String toString() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    this.buildJson(builder);
    return JsonUtils.toJsonText(builder, true);
  }

}
