package pascaldimassimo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilsTest
{
    @Test
    public void test_computeLevenshteinDistance() throws Exception
    {
        final String str1 = "adobe";
        final String str2 = "adobe photoshop 7";

        assertEquals(2, Utils.computeLevenshteinDistance(str1.split("\\s"), str2.split("\\s")));
    }

    @Test
    public void test_computeLevenshteinDistanceSimilarity() throws Exception
    {
        final String str1 = "adobe";
        final String str2 = "adobe photoshop 7";

        assertEquals(1 / 3d, Utils.computeLevenshteinDistanceSimilarity(str1, str2), 0.0001);
    }

    @Test
    public void test_computeLevenshteinDistance_deletion() throws Exception
    {
        final String str1 = "adobe photoshop 7";
        final String str2 = "adobe photoshop";

        assertEquals(1, Utils.computeLevenshteinDistance(str1.split("\\s"), str2.split("\\s")));
    }

    @Test
    public void test_computeLevenshteinDistance_insertion() throws Exception
    {
        final String str1 = "adobe photoshop";
        final String str2 = "adobe 7 photoshop";

        assertEquals(1, Utils.computeLevenshteinDistance(str1.split("\\s"), str2.split("\\s")));
    }

    @Test
    public void test_computeLevenshteinDistance_substitution() throws Exception
    {
        final String str1 = "adobe photoshop";
        final String str2 = "adobe flash";

        assertEquals(1, Utils.computeLevenshteinDistance(str1.split("\\s"), str2.split("\\s")));
    }
}
