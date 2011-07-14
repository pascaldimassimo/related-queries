package pascaldimassimo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.common.Pair;
import org.apache.mahout.fpm.pfpgrowth.convertors.string.TopKStringPatterns;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

public class PatternsReader
{
    public static void main(String[] args) throws Exception
    {
        if (args.length != 4)
        {
            System.err.println("Usage: <input> <solr-host> <solr-port> <solr-core>");
            System.exit(2);
        }

        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);

        String input = args[0];
        Path path = new Path(input);
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);

        Text key = new Text();
        TopKStringPatterns value = new TopKStringPatterns();

        String host = args[1];
        int port = Integer.parseInt(args[2]);
        String core = args[3];
        String url = String.format("http://%s:%d/solr/%s", host, port, core);
        CommonsHttpSolrServer server = new CommonsHttpSolrServer(url);

        while (reader.next(key, value))
        {
            System.out.println("Processing " + key);
            Set<String> set = buildRelatedSet(key, value);
            SolrInputDocument doc = buildDocument(key, set);
            server.add(doc);
        }

        server.commit();
    }

    private static SolrInputDocument buildDocument(Text key, Set<String> set)
    {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("query", key.toString());
        for (String related : set)
        {
            doc.addField("related", related);
        }
        return doc;
    }

    private static Set<String> buildRelatedSet(Text key, TopKStringPatterns value)
    {
        Set<String> relateds = new HashSet<String>();
        List<Pair<List<String>, Long>> patterns = value.getPatterns();
        for (Pair<List<String>, Long> pair : patterns)
        {
            List<String> list = pair.getFirst();
            for (String string : list)
            {
                if (!string.equals(key.toString()))
                {
                    relateds.add(string);
                }
            }
        }
        return relateds;
    }
}
