package pascaldimassimo;

import static org.junit.Assert.assertEquals;

import java.util.GregorianCalendar;

import org.junit.Test;

public class QueryTest
{
    @Test
    public void test_construct_from_log() throws Exception
    {
        String log = "142\trentdirect.com\t2006-03-01 07:17:12";
        
        Query query = new Query(log);
        assertEquals("142", query.getUserID());
        assertEquals("rentdirect.com", query.getQueryString());
        assertEquals(new GregorianCalendar(2006, 2, 1, 7, 17, 12).getTime(), query.getTimestamp());
    }
}
