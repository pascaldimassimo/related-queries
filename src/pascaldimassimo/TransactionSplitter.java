package pascaldimassimo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class TransactionSplitter extends Configured implements Tool
{
    @Override
    public int run(String[] args) throws Exception
    {
        if (args.length != 2)
        {
            System.err.println("Usage: " + getClass().getName() + "<input> <output>");
            System.exit(2);
        }

        Configuration conf = getConf();

        // Creating the MapReduce job (configuration) object
        Job job = new Job(conf);
        job.setJarByClass(getClass());
        job.setJobName(getClass().getName());

        job.setMapperClass(TransactionMapper.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setReducerClass(TransactionReducer.class);

        // This is what the Mapper will be outputting to the Reducer
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // Setting the input folder of the job
        FileInputFormat.addInputPath(job, new Path(args[0]));

        // Preparing the output folder by first deleting it if it exists
        Path output = new Path(args[1]);
        FileSystem.get(conf).delete(output, true);
        FileOutputFormat.setOutputPath(job, output);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception
    {
        int result = ToolRunner.run(new Configuration(), new TransactionSplitter(), args);
        System.exit(result);
    }

}
