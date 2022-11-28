package uk.me.berndporr.kiss_fft;

import uk.me.berndporr.kiss_fft.KISSFastFourierTransformer;

import static junit.framework.Assert.assertFalse;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class LoadTest {

    public void useAppContext() throws Exception {
        KISSFastFourierTransformer kissFastFourierTransformer = new KISSFastFourierTransformer();
        assertFalse(kissFastFourierTransformer == null);
    }
}
