/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package org.kiji.express.wikimedia;  
@SuppressWarnings("all")
/** * A custom Avro record to hold complex types for the 'reverting_edits' column of the Wikimedia
    * 'page' table. Specifically, reverting edits are an array of longs. */
@org.apache.avro.specific.AvroGenerated
public class RevertingEditsRecord extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"RevertingEditsRecord\",\"namespace\":\"org.kiji.express.wikimedia\",\"doc\":\"* A custom Avro record to hold complex types for the 'reverting_edits' column of the Wikimedia\\n    * 'page' table. Specifically, reverting edits are an array of longs.\",\"fields\":[{\"name\":\"reverting_edits\",\"type\":{\"type\":\"array\",\"items\":\"long\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.util.List<java.lang.Long> reverting_edits;

  /**
   * Default constructor.
   */
  public RevertingEditsRecord() {}

  /**
   * All-args constructor.
   */
  public RevertingEditsRecord(java.util.List<java.lang.Long> reverting_edits) {
    this.reverting_edits = reverting_edits;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return reverting_edits;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: reverting_edits = (java.util.List<java.lang.Long>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'reverting_edits' field.
   */
  public java.util.List<java.lang.Long> getRevertingEdits() {
    return reverting_edits;
  }

  /**
   * Sets the value of the 'reverting_edits' field.
   * @param value the value to set.
   */
  public void setRevertingEdits(java.util.List<java.lang.Long> value) {
    this.reverting_edits = value;
  }

  /** Creates a new RevertingEditsRecord RecordBuilder */
  public static org.kiji.express.wikimedia.RevertingEditsRecord.Builder newBuilder() {
    return new org.kiji.express.wikimedia.RevertingEditsRecord.Builder();
  }
  
  /** Creates a new RevertingEditsRecord RecordBuilder by copying an existing Builder */
  public static org.kiji.express.wikimedia.RevertingEditsRecord.Builder newBuilder(org.kiji.express.wikimedia.RevertingEditsRecord.Builder other) {
    return new org.kiji.express.wikimedia.RevertingEditsRecord.Builder(other);
  }
  
  /** Creates a new RevertingEditsRecord RecordBuilder by copying an existing RevertingEditsRecord instance */
  public static org.kiji.express.wikimedia.RevertingEditsRecord.Builder newBuilder(org.kiji.express.wikimedia.RevertingEditsRecord other) {
    return new org.kiji.express.wikimedia.RevertingEditsRecord.Builder(other);
  }
  
  /**
   * RecordBuilder for RevertingEditsRecord instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<RevertingEditsRecord>
    implements org.apache.avro.data.RecordBuilder<RevertingEditsRecord> {

    private java.util.List<java.lang.Long> reverting_edits;

    /** Creates a new Builder */
    private Builder() {
      super(org.kiji.express.wikimedia.RevertingEditsRecord.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.kiji.express.wikimedia.RevertingEditsRecord.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing RevertingEditsRecord instance */
    private Builder(org.kiji.express.wikimedia.RevertingEditsRecord other) {
            super(org.kiji.express.wikimedia.RevertingEditsRecord.SCHEMA$);
      if (isValidValue(fields()[0], other.reverting_edits)) {
        this.reverting_edits = data().deepCopy(fields()[0].schema(), other.reverting_edits);
        fieldSetFlags()[0] = true;
      }
    }

    /** Gets the value of the 'reverting_edits' field */
    public java.util.List<java.lang.Long> getRevertingEdits() {
      return reverting_edits;
    }
    
    /** Sets the value of the 'reverting_edits' field */
    public org.kiji.express.wikimedia.RevertingEditsRecord.Builder setRevertingEdits(java.util.List<java.lang.Long> value) {
      validate(fields()[0], value);
      this.reverting_edits = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'reverting_edits' field has been set */
    public boolean hasRevertingEdits() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'reverting_edits' field */
    public org.kiji.express.wikimedia.RevertingEditsRecord.Builder clearRevertingEdits() {
      reverting_edits = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    public RevertingEditsRecord build() {
      try {
        RevertingEditsRecord record = new RevertingEditsRecord();
        record.reverting_edits = fieldSetFlags()[0] ? this.reverting_edits : (java.util.List<java.lang.Long>) defaultValue(fields()[0]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
