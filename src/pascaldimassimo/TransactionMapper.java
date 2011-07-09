package pascaldimassimo;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class TransactionMapper extends Mapper<LongWritable, Text, Text, Text>
{
    /**
     * The maximal interval length allowed between the adjacent query records in
     * a same query transaction
     * 
     * Set to 5 minutes (in ms)
     */
    private static final long   MAX_TRANSACTION_INTERVAL_LENGTH                         = 5 * 60 * 1000;

    /**
     * The maximal interval length of the period during which the user is
     * allowed to be inactive. If the time between two adjacent query records in
     * a same user session is larger than max_inactive_interval_length, then the
     * later query record definitely indicates the start of a novel query
     * transaction
     * 
     * Set to 24h (in ms)
     */
    private static final long   MAX_INACTIVE_INTERVAL_LENGTH                            = 24 * 60 * 60 * 1000;

    /**
     * The maximal length of the time window that the query transaction is
     * allowed to span. This constraint bounds the period during which the user
     * is focused on a single topic or strongly related topics
     * 
     * Set to 1h (in ms)
     */
    private static final long   MAX_TRANSACTION_TIME_WINDOW_LENGTH                      = 60 * 60 * 1000;

    /**
     * We utilize the similarity between adjacent queries at the hypothesized
     * boundary of query transactions in a same user session to determine
     * whether they should belong to the same query transaction. We employ the
     * Levenshtein distance similarity to measure the surface similarity between
     * two queries. If their Levenshtein distance similarity is above the
     * threshold min_levenshtein_distance_similarity_ for_related_queries,
     * simply treat the later one as the modified version of the former one and
     * include them in the same query transaction; otherwise separate them into
     * two query transactions.
     * 
     * Set to 1/3
     */
    private static final double MIN_LEVENSHTEIN_DISTANCE_SIMILARITY_FOR_RELATED_QUERIES = 1 / 3d;

    private Text                currentTransactionKey;

    private Date                currentTransactionStartTime;

    private Query               previousQuery;

    @Override
    protected void map(final LongWritable key, final Text value, final Context context) throws IOException, InterruptedException
    {
        // Skip empty and header line
        if (value.toString().isEmpty() || value.toString().charAt(0) == 'A')
        {
            return;
        }

        Query query = new Query(value.toString());

        // Skip '-'
        if (query.getQueryString().equals("-"))
        {
            return;
        }

        // First run or new user?
        if (currentTransactionKey == null || !query.getUserID().equals(previousQuery.getUserID()))
        {
            initNewTransaction(query);
            appendQueryToTransaction(query, context);
            previousQuery = query;
            return;
        }

        // Same query string?
        if (query.getQueryString().equals(previousQuery.getQueryString()))
        {
            // Skip this query
            previousQuery = query;
            return;
        }

        long deltaPreviousQuery = compareDate(query.getTimestamp(), previousQuery.getTimestamp());
        long deltaTransactionStartTime = compareDate(query.getTimestamp(), currentTransactionStartTime);

        // Same transaction?
        if (deltaPreviousQuery <= MAX_TRANSACTION_INTERVAL_LENGTH && deltaTransactionStartTime <= MAX_TRANSACTION_TIME_WINDOW_LENGTH)
        {
            appendQueryToTransaction(query, context);
            previousQuery = query;
            return;
        }

        // Inactive for too long?
        if (deltaPreviousQuery > MAX_INACTIVE_INTERVAL_LENGTH)
        {
            initNewTransaction(query);
            appendQueryToTransaction(query, context);
            previousQuery = query;
            return;
        }

        double similarity = Utils.computeLevenshteinDistanceSimilarity(previousQuery.getQueryString(), query.getQueryString());

        // Queries not similar enough?
        if (similarity < MIN_LEVENSHTEIN_DISTANCE_SIMILARITY_FOR_RELATED_QUERIES)
        {
            initNewTransaction(query);
        }
        // else: append query to current transaction

        appendQueryToTransaction(query, context);
        previousQuery = query;
    }

    private void appendQueryToTransaction(Query query, final Context context) throws IOException, InterruptedException
    {
        context.write(currentTransactionKey, new Text(query.getQueryString()));
    }

    private void initNewTransaction(Query query)
    {
        currentTransactionKey = new Text(query.getUserID() + ":" + query.getTimestamp().getTime());
        currentTransactionStartTime = query.getTimestamp();
    }

    private long compareDate(Date date1, Date date2)
    {
        return date1.getTime() - date2.getTime();
    }

}
