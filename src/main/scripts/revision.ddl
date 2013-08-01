CREATE TABLE revision WITH DESCRIPTION 'Revisions of Wikipedia pages.'
ROW KEY FORMAT HASH PREFIXED(2)
WITH LOCALITY GROUP default (
    MAXVERSIONS = infinity,
    TTL = infinity,
    INMEMORY = false,
    COMPRESSED WITH gzip,
    GROUP TYPE FAMILY info WITH DESCRIPTION 'Basic data about the revision.' (
        meta_data WITH SCHEMA CLASS com.wibidata.wikimedia.avro.RevMetaData WITH DESCRIPTION
            'Revision meta-data.',
        editor_id WITH SCHEMA CLASS com.wibidata.wikimedia.avro.EditorId WITH DESCRIPTION
            'Identifies the editor who made the revision.',
        comment "string" WITH DESCRIPTION 'The comment given by the editor when submitting the
            revision.',
        delta "string" WITH DESCRIPTION 'String encoding of the delta between raw before and
            after revision texts.',
        delta_no_templates "string" WITH DESCRIPTION 'String encoding of the delta between before
            and after revision texts, with the templates stripped from them.',
        before_text "string" WITH DESCRIPTION 'The full text of the last revision.'
    ),
    GROUP TYPE FAMILY edit_metrics WITH DESCRIPTION 'Metrics about content changes made in
        revisions.' (
        delta "long" WITH DESCRIPTION 'Number of characters inserted, less number deleted, in the
            revision.',
        delta_no_templates "long" WITH DESCRIPTION 'Number of characters inserted in the revision,
            less number deleted, discounting changes in templates.'
    ),
    GROUP TYPE FAMILY templates WITH DESCRIPTION 'Derived info about templates.' (
        before_templates WITH SCHEMA CLASS com.wibidata.wikimedia.avro.TemplateRecordArray
            WITH DESCRIPTION 'Array of records for all templates found in beforeText.',
        after_templates WITH SCHEMA CLASS com.wibidata.wikimedia.avro.TemplateRecordArray
            WITH DESCRIPTION 'Array of records for all templates found in afterText.'
    ),
    GROUP TYPE FAMILY revert_type WITH DESCRIPTION 'Flags about whether the revision reverted or
        was reverted by another revision.' (
        is_reverting "boolean" WITH DESCRIPTION 'If true, this revision restored the page to an
            earlier state.',
        is_reverted "boolean" WITH DESCRIPTION 'If true, this revision was later undone by a
            different revision.'
    )
);

