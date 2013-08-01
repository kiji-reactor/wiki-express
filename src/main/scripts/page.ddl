CREATE TABLE page WITH DESCRIPTION 'Wikipedia page info.'
ROW KEY FORMAT HASH PREFIXED(2)
WITH LOCALITY GROUP default (
    MAXVERSIONS = infinity,
    TTL = infinity,
    INMEMORY = false,
    COMPRESSED WITH gzip,
    GROUP TYPE FAMILY info WITH DESCRIPTION 'Basic data about the page.' (
        COLUMN page_title WITH SCHEMA "string" WITH DESCRIPTION 'Title of the page.'
    ),
    MAP TYPE FAMILY summary_rev_info WITH SCHEMA CLASS com.wibidata.wikimedia.avro.SummaryRevInfo
        WITH DESCRIPTION 'Information about each revision to the page, where revision IDs are
        qualifiers.',
    MAP TYPE FAMILY edit_metrics WITH SCHEMA CLASS com.wibidata.wikimedia.avro.UserTableMetricRecord        WITH DESCRIPTION 'Metrics on revisions made by the editor.'
),
LOCALITY GROUP only_need_one_copy (
    MAXVERSIONS = 1,
    TTL = infinity,
    INMEMORY = false,
    COMPRESSED WITH gzip,
    GROUP TYPE FAMILY derived WITH DESCRIPTION 'The columns in our table that we need only one copy
        of a cell.' (
        recent_text "string" WITH DESCRIPTION 'The most recent version of the page.',
        categories WITH SCHEMA CLASS org.kiji.examples.wikimedia.CategoriesRecord WITH DESCRIPTION
            'Categories associated with the page.',
        links "string" WITH DESCRIPTION 'Titles of wikipages linked to.',
        reverted_edits WITH SCHEMA CLASS org.kiji.examples.wikimedia.RevertedEditsRecord WITH
            DESCRIPTION 'Edits to this page that have since been reverted.',
        reverting_edits WITH SCHEMA CLASS org.kiji.examples.wikimedia.RevertingEditsRecord WITH
            DESCRIPTION 'Edits to this page that are restoring it to a previous state.'
    )
);

