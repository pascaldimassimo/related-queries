package pascaldimassimo;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class TransactionReducer extends Reducer<Text, Text, Text, Text>
{
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
    {
        int n = 0;
        StringBuilder sb = new StringBuilder();
        Iterator<Text> iter = values.iterator();
        while (iter.hasNext())
        {
            n++;
            sb.append(iter.next().toString());
            if (iter.hasNext())
            {
                sb.append(",");
            }
        }

        // Skip transaction with less than 2 queries
        if (n < 2)
        {
            return;
        }

        // Format for values is: query1<COMMA>query2<COMMA>...
        context.write(key, new Text(sb.toString()));
    }

}
