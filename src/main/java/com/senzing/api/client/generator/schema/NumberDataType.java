package com.senzing.api.client.generator.schema;

import java.util.Objects;

/**
 * Extends {@link ApiDataType} to describe a numeric type and its properties.
 */
public abstract class NumberDataType<T extends Number> extends ApiDataType {
  /**
   * Whether or not the minimum is exclusive.
   */
  private boolean exclusiveMinimum = false;

  /**
   * Whether or not the maximum is exclusive.
   */
  private boolean exclusiveMaximum = false;

  /**
   * The minimum value for the data type, or <tt>null</tt> if none.
   */
  private T minimum = null;

  /**
   * The maximum value for the data type, or <tt>null</tt> if none.
   */
  private T maximum = null;

  /**
   * If not <tt>null</tt> then values for this type must be multiples of this
   * value.
   */
  private T multipleOf = null;

  /**
   * Default constructor for constructing an anonymous type.
   */
  public NumberDataType() {
    super();
  }

  /**
   * Constructs a named type.
   */
  public NumberDataType(String name) {
    super(name);
  }

  /**
   * Checks if the minimum value is exclusive.  This defaults to <tt>false</tt>
   * if it has never been set.
   *
   * @return <tt>true</tt> if the minimum value is exclusive, otherwise
   *         <tt>false</tt>.
   */
  public boolean isExclusiveMinimum() {
    return this.exclusiveMinimum;
  }

  /**
   * Sets whether or not the minimum value is exclusive.  This defaults to
   * <tt>false</tt> if it has never been set.
   *
   * @param exclusive <tt>true</tt> if the minimum value is exclusive, otherwise
   *                  <tt>false</tt>.
   */
  public void setExclusiveMinimum(boolean exclusive) {
    this.exclusiveMinimum = exclusive;
  }

  /**
   * Checks if the maximum value is exclusive.  This defaults to <tt>false</tt>
   * if it has never been set.
   *
   * @return <tt>true</tt> if the maximum value is exclusive, otherwise
   *         <tt>false</tt>.
   */
  public boolean isExclusiveMaximum() {
    return this.exclusiveMaximum;
  }

  /**
   * Sets whether or not the maximum value is exclusive.  This defaults to
   * <tt>false</tt> if it has never been set.
   *
   * @param exclusive <tt>true</tt> if the maximum value is exclusive, otherwise
   *                  <tt>false</tt>.
   */
  public void setExclusiveMaximum(boolean exclusive) {
    this.exclusiveMaximum = exclusive;
  }

  /**
   * Gets the minimum for values of this type (if any).  This returns
   * <tt>null</tt> if no minimum.
   *
   * @return The minimum for values of this type, or <tt>null</tt> if no
   *         minimum.
   */
  public T getMinimum() {
    return this.minimum;
  }

  /**
   * Sets the minimum for values of this type (if any).  Set this to
   * <tt>null</tt> if no minimum.
   *
   * @param minimum The minimum for values of this type, or <tt>null</tt> if no
   *                minimum.
   */
  public void setMinimum(T minimum) {
    this.minimum = minimum;
  }

  /**
   * Gets the maximum for values of this type (if any).  This returns
   * <tt>null</tt> if no maximum.
   *
   * @return The maximum for values of this type, or <tt>null</tt> if no
   *         maximum.
   */
  public T getMaximum() {
    return this.maximum;
  }

  /**
   * Sets the maximum for values of this type (if any).  Set this to
   * <tt>null</tt> if no maximum.
   *
   * @param maximum The maximum for values of this type, or <tt>null</tt> if no
   *                maximum.
   */
  public void setMaximum(T maximum) {
    this.maximum = maximum;
  }

  /**
   * Gets the common denominator for all values of this type (if any).  This
   * returns <tt>null</tt> if the values are not a multiple of a specific value.
   *
   * @return The common denominator for all values of this type (if any), or
   *         <tt>null</tt> if the values are not a multiple of a specific value.
   */
  public T getMultipleOf() {
    return this.multipleOf;
  }

  /**
   * Sets the common denominator for all values of this type (if any).  Set this
   * to <tt>null</tt> if the values are not a multiple of a specific value.
   *
   * @param multipleOf The common denominator for all values of this type (if
   *                   any), or <tt>null</tt> if the values are not a multiple
   *                   of a specific value.
   */
  public void setMultipleOf(T multipleOf) {
    this.multipleOf = multipleOf;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;
    NumberDataType that = (NumberDataType) object;
    return this.isExclusiveMinimum() == that.isExclusiveMinimum() &&
        this.isExclusiveMaximum() == that.isExclusiveMaximum() &&
        Objects.equals(this.getMinimum(), that.getMinimum()) &&
        Objects.equals(this.getMaximum(), that.getMaximum()) &&
        Objects.equals(this.getMultipleOf(), that.getMultipleOf());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(),
                        this.isExclusiveMinimum(),
                        this.isExclusiveMaximum(),
                        this.getMinimum(),
                        this.getMaximum(),
                        this.getMultipleOf());
  }
}
