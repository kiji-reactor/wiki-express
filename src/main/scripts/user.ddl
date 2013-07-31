CREATE TABLE user WITH DESCRIPTION 'Data on Wikipedia users and revisions they\'ve made.'
ROW KEY FORMAT HASHED
WITH LOCALITY GROUP default (
    MAXVERSIONS = infinity,
    TTL = infinity,
    INMEMORY = false,
    COMPRESSED WITH gzip,
    GROUP TYPE FAMILY info WITH DESCRIPTION 'Data on editors and revisions they\'ve made.' (
        COLUMN user WITH SCHEMA "string" WITH DESCRIPTION 'A friendly identifier for the user.',
        page_title "string" WITH DESCRIPTION 'The title of a page revised by the editor.',
        user_type "string" WITH DESCRIPTION 'The type of user: BOT, USER, or IP.',
        total_edits "int" WITH DESCRIPTION 'The total number of edits made by this user.',
        user_start_time "long" WITH DESCRIPTION 'The time of the user\'s first edit.'
    ),
    MAP TYPE FAMILY edit_metrics WITH SCHEMA CLASS com.wibidata.wikimedia.avro.UserTableMetricRecord
        WITH DESCRIPTION 'Metrics on revisions made by the editor.',
    MAP TYPE FAMILY edit_metric_stats WITH SCHEMA CLASS
        com.wibidata.wikimedia.avro.DescriptiveStatsRecord WITH DESCRIPTION 'Descriptive
        statistics relating to metrics computed on edits.',
    GROUP TYPE FAMILY time_series WITH DESCRIPTION 'Time series derived from the user
        edit_metrics.' (
        is_reverted "double" WITH DESCRIPTION 'Per bin, the number of edits a user has made that
            have been subsequently reverted.',
        is_reverting "double" WITH DESCRIPTION 'Per bin, the number of edits a user has made that
            have reverted an earlier edit.',
        is_only_in_templates "double" WITH DESCRIPTION 'Per bin, the number of edits a user has
            made that only add or remove templates.',
        delta_no_templates "double" WITH DESCRIPTION 'Per bin, the summed delta
            (charsAdded-charsDeleted) a user has made, discounting characters added in templates.',
        delta_new_content "double" WITH DESCRIPTION 'Per bin, the summed delta
            (charsAdded-charsDeleted) a user has made from edits that are neither reverted or
            reverting, discounting characters added in templates.',
        total_edits "double" WITH DESCRIPTION 'Per bin, the total number of edits made by a user.',
        total_non_revert_edits "double" WITH DESCRIPTION 'Per bin, the total number of edits made
            by a user that were neither reverted or reverting.'
    )
);

