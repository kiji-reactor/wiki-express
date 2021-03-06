/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.wibidata.wikimedia.avro;  
@SuppressWarnings("all")
/** A backup record of one revision of an article. */
@org.apache.avro.specific.AvroGenerated
public class PageRevision extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"PageRevision\",\"namespace\":\"com.wibidata.wikimedia.avro\",\"doc\":\"A backup record of one revision of an article.\",\"fields\":[{\"name\":\"metaData\",\"type\":{\"type\":\"record\",\"name\":\"RevMetaData\",\"doc\":\"Basic meta-data about a revision.\",\"fields\":[{\"name\":\"id\",\"type\":\"long\",\"doc\":\"Unique identifier for this revision.\"},{\"name\":\"pageId\",\"type\":\"long\",\"doc\":\"Unique identifier of the page revised.\"},{\"name\":\"pageTitle\",\"type\":\"string\",\"doc\":\"Title of the page revised.\"},{\"name\":\"timestamp\",\"type\":\"long\",\"doc\":\"The timestamp of the revision.\"},{\"name\":\"isMinor\",\"type\":\"boolean\",\"doc\":\"True if the revision was marked as minor.\"}],\"aliases\":[\"com.odiago.wikimedia.avro.RevMetaData\"]},\"doc\":\"Basic meta-data about a revision.\"},{\"name\":\"editorId\",\"type\":{\"type\":\"record\",\"name\":\"EditorId\",\"doc\":\"Identifying data known for an editor.\",\"fields\":[{\"name\":\"idType\",\"type\":{\"type\":\"enum\",\"name\":\"IdTypes\",\"symbols\":[\"UNAME\",\"IP\",\"UNKNOWN\"],\"aliases\":[\"com.odiago.wikimedia.avro.IdTypes\"]},\"doc\":\"The type of this identifier, user name, user ip, or unknown.\"},{\"name\":\"username\",\"type\":[\"null\",\"string\"],\"doc\":\"Username for registered contributor.\"},{\"name\":\"userId\",\"type\":[\"null\",\"long\"],\"doc\":\"User ID (comes with username) of registered contributor.\"},{\"name\":\"ip\",\"type\":[\"null\",\"string\"],\"doc\":\"IP address of anonymous contributor.\"}],\"aliases\":[\"com.odiago.wikimedia.avro.EditorId\"]},\"doc\":\"Identifying data known for the editor that last edited this article.\"},{\"name\":\"beforeText\",\"type\":\"string\",\"doc\":\"The full text of the last revision.\"},{\"name\":\"afterText\",\"type\":\"string\",\"doc\":\"The full text of this revision.\"},{\"name\":\"delta\",\"type\":\"string\",\"doc\":\"Delta applied to previous revision text to obtain this revision's text.\"},{\"name\":\"comment\",\"type\":[\"null\",\"string\"],\"doc\":\"The comment given by the editor when submitting the revision.\"}],\"aliases\":[\"com.odiago.wikimedia.avro.PageRevision\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  /** Basic meta-data about a revision. */
  @Deprecated public com.wibidata.wikimedia.avro.RevMetaData metaData;
  /** Identifying data known for the editor that last edited this article. */
  @Deprecated public com.wibidata.wikimedia.avro.EditorId editorId;
  /** The full text of the last revision. */
  @Deprecated public java.lang.CharSequence beforeText;
  /** The full text of this revision. */
  @Deprecated public java.lang.CharSequence afterText;
  /** Delta applied to previous revision text to obtain this revision's text. */
  @Deprecated public java.lang.CharSequence delta;
  /** The comment given by the editor when submitting the revision. */
  @Deprecated public java.lang.CharSequence comment;

  /**
   * Default constructor.
   */
  public PageRevision() {}

  /**
   * All-args constructor.
   */
  public PageRevision(com.wibidata.wikimedia.avro.RevMetaData metaData, com.wibidata.wikimedia.avro.EditorId editorId, java.lang.CharSequence beforeText, java.lang.CharSequence afterText, java.lang.CharSequence delta, java.lang.CharSequence comment) {
    this.metaData = metaData;
    this.editorId = editorId;
    this.beforeText = beforeText;
    this.afterText = afterText;
    this.delta = delta;
    this.comment = comment;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return metaData;
    case 1: return editorId;
    case 2: return beforeText;
    case 3: return afterText;
    case 4: return delta;
    case 5: return comment;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: metaData = (com.wibidata.wikimedia.avro.RevMetaData)value$; break;
    case 1: editorId = (com.wibidata.wikimedia.avro.EditorId)value$; break;
    case 2: beforeText = (java.lang.CharSequence)value$; break;
    case 3: afterText = (java.lang.CharSequence)value$; break;
    case 4: delta = (java.lang.CharSequence)value$; break;
    case 5: comment = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'metaData' field.
   * Basic meta-data about a revision.   */
  public com.wibidata.wikimedia.avro.RevMetaData getMetaData() {
    return metaData;
  }

  /**
   * Sets the value of the 'metaData' field.
   * Basic meta-data about a revision.   * @param value the value to set.
   */
  public void setMetaData(com.wibidata.wikimedia.avro.RevMetaData value) {
    this.metaData = value;
  }

  /**
   * Gets the value of the 'editorId' field.
   * Identifying data known for the editor that last edited this article.   */
  public com.wibidata.wikimedia.avro.EditorId getEditorId() {
    return editorId;
  }

  /**
   * Sets the value of the 'editorId' field.
   * Identifying data known for the editor that last edited this article.   * @param value the value to set.
   */
  public void setEditorId(com.wibidata.wikimedia.avro.EditorId value) {
    this.editorId = value;
  }

  /**
   * Gets the value of the 'beforeText' field.
   * The full text of the last revision.   */
  public java.lang.CharSequence getBeforeText() {
    return beforeText;
  }

  /**
   * Sets the value of the 'beforeText' field.
   * The full text of the last revision.   * @param value the value to set.
   */
  public void setBeforeText(java.lang.CharSequence value) {
    this.beforeText = value;
  }

  /**
   * Gets the value of the 'afterText' field.
   * The full text of this revision.   */
  public java.lang.CharSequence getAfterText() {
    return afterText;
  }

  /**
   * Sets the value of the 'afterText' field.
   * The full text of this revision.   * @param value the value to set.
   */
  public void setAfterText(java.lang.CharSequence value) {
    this.afterText = value;
  }

  /**
   * Gets the value of the 'delta' field.
   * Delta applied to previous revision text to obtain this revision's text.   */
  public java.lang.CharSequence getDelta() {
    return delta;
  }

  /**
   * Sets the value of the 'delta' field.
   * Delta applied to previous revision text to obtain this revision's text.   * @param value the value to set.
   */
  public void setDelta(java.lang.CharSequence value) {
    this.delta = value;
  }

  /**
   * Gets the value of the 'comment' field.
   * The comment given by the editor when submitting the revision.   */
  public java.lang.CharSequence getComment() {
    return comment;
  }

  /**
   * Sets the value of the 'comment' field.
   * The comment given by the editor when submitting the revision.   * @param value the value to set.
   */
  public void setComment(java.lang.CharSequence value) {
    this.comment = value;
  }

  /** Creates a new PageRevision RecordBuilder */
  public static com.wibidata.wikimedia.avro.PageRevision.Builder newBuilder() {
    return new com.wibidata.wikimedia.avro.PageRevision.Builder();
  }
  
  /** Creates a new PageRevision RecordBuilder by copying an existing Builder */
  public static com.wibidata.wikimedia.avro.PageRevision.Builder newBuilder(com.wibidata.wikimedia.avro.PageRevision.Builder other) {
    return new com.wibidata.wikimedia.avro.PageRevision.Builder(other);
  }
  
  /** Creates a new PageRevision RecordBuilder by copying an existing PageRevision instance */
  public static com.wibidata.wikimedia.avro.PageRevision.Builder newBuilder(com.wibidata.wikimedia.avro.PageRevision other) {
    return new com.wibidata.wikimedia.avro.PageRevision.Builder(other);
  }
  
  /**
   * RecordBuilder for PageRevision instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<PageRevision>
    implements org.apache.avro.data.RecordBuilder<PageRevision> {

    private com.wibidata.wikimedia.avro.RevMetaData metaData;
    private com.wibidata.wikimedia.avro.EditorId editorId;
    private java.lang.CharSequence beforeText;
    private java.lang.CharSequence afterText;
    private java.lang.CharSequence delta;
    private java.lang.CharSequence comment;

    /** Creates a new Builder */
    private Builder() {
      super(com.wibidata.wikimedia.avro.PageRevision.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(com.wibidata.wikimedia.avro.PageRevision.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing PageRevision instance */
    private Builder(com.wibidata.wikimedia.avro.PageRevision other) {
            super(com.wibidata.wikimedia.avro.PageRevision.SCHEMA$);
      if (isValidValue(fields()[0], other.metaData)) {
        this.metaData = data().deepCopy(fields()[0].schema(), other.metaData);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.editorId)) {
        this.editorId = data().deepCopy(fields()[1].schema(), other.editorId);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.beforeText)) {
        this.beforeText = data().deepCopy(fields()[2].schema(), other.beforeText);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.afterText)) {
        this.afterText = data().deepCopy(fields()[3].schema(), other.afterText);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.delta)) {
        this.delta = data().deepCopy(fields()[4].schema(), other.delta);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.comment)) {
        this.comment = data().deepCopy(fields()[5].schema(), other.comment);
        fieldSetFlags()[5] = true;
      }
    }

    /** Gets the value of the 'metaData' field */
    public com.wibidata.wikimedia.avro.RevMetaData getMetaData() {
      return metaData;
    }
    
    /** Sets the value of the 'metaData' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder setMetaData(com.wibidata.wikimedia.avro.RevMetaData value) {
      validate(fields()[0], value);
      this.metaData = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'metaData' field has been set */
    public boolean hasMetaData() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'metaData' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder clearMetaData() {
      metaData = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'editorId' field */
    public com.wibidata.wikimedia.avro.EditorId getEditorId() {
      return editorId;
    }
    
    /** Sets the value of the 'editorId' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder setEditorId(com.wibidata.wikimedia.avro.EditorId value) {
      validate(fields()[1], value);
      this.editorId = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'editorId' field has been set */
    public boolean hasEditorId() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'editorId' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder clearEditorId() {
      editorId = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'beforeText' field */
    public java.lang.CharSequence getBeforeText() {
      return beforeText;
    }
    
    /** Sets the value of the 'beforeText' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder setBeforeText(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.beforeText = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'beforeText' field has been set */
    public boolean hasBeforeText() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'beforeText' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder clearBeforeText() {
      beforeText = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'afterText' field */
    public java.lang.CharSequence getAfterText() {
      return afterText;
    }
    
    /** Sets the value of the 'afterText' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder setAfterText(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.afterText = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'afterText' field has been set */
    public boolean hasAfterText() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'afterText' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder clearAfterText() {
      afterText = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'delta' field */
    public java.lang.CharSequence getDelta() {
      return delta;
    }
    
    /** Sets the value of the 'delta' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder setDelta(java.lang.CharSequence value) {
      validate(fields()[4], value);
      this.delta = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'delta' field has been set */
    public boolean hasDelta() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'delta' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder clearDelta() {
      delta = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /** Gets the value of the 'comment' field */
    public java.lang.CharSequence getComment() {
      return comment;
    }
    
    /** Sets the value of the 'comment' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder setComment(java.lang.CharSequence value) {
      validate(fields()[5], value);
      this.comment = value;
      fieldSetFlags()[5] = true;
      return this; 
    }
    
    /** Checks whether the 'comment' field has been set */
    public boolean hasComment() {
      return fieldSetFlags()[5];
    }
    
    /** Clears the value of the 'comment' field */
    public com.wibidata.wikimedia.avro.PageRevision.Builder clearComment() {
      comment = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    @Override
    public PageRevision build() {
      try {
        PageRevision record = new PageRevision();
        record.metaData = fieldSetFlags()[0] ? this.metaData : (com.wibidata.wikimedia.avro.RevMetaData) defaultValue(fields()[0]);
        record.editorId = fieldSetFlags()[1] ? this.editorId : (com.wibidata.wikimedia.avro.EditorId) defaultValue(fields()[1]);
        record.beforeText = fieldSetFlags()[2] ? this.beforeText : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.afterText = fieldSetFlags()[3] ? this.afterText : (java.lang.CharSequence) defaultValue(fields()[3]);
        record.delta = fieldSetFlags()[4] ? this.delta : (java.lang.CharSequence) defaultValue(fields()[4]);
        record.comment = fieldSetFlags()[5] ? this.comment : (java.lang.CharSequence) defaultValue(fields()[5]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
