package it.fcambi.news.model.tasks;

import it.fcambi.news.async.Task;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by Francesco on 04/11/15.
 */
public class PastTaskTest {

    private Task t;

    @Before
    public void setUp() {
        t = new Task() {
            @Override
            public String getName() {
                return "Test Task";
            }

            @Override
            public String getDescription() {
                return "A Task for testing";
            }

            @Override
            protected void executeTask() throws Exception {
                throw new Exception("Simulated fail");
            }
        };
        Date now = new Date();
        t.setCreator(this.getClass().getName());

        t.setScheduleTime(now);
        t.run();
    }

    @Test
    public void createFromTest() {

        PastTask p = PastTask.createFrom(t);

        assertEquals(t.getName(), p.getName());
        assertEquals(String.format("Scheduled @ %Tc; Task created by %s", t.getScheduleTime(), t.getCreator()), p.getDescription());
        assertEquals(t.getCreationTime(), p.getCreationTime());
        assertEquals(t.getStartTime(), p.getStartTime());
        assertEquals(t.getEndTime(), p.getEndTime());
        assertEquals("FAILED", p.getResult());
        assertEquals(String.format("Exception at %.4f %% because %s%n%s",
                t.getProgress(), t.getException().toString(),
                Arrays.toString(t.getException().getStackTrace())), p.getDetailedResult());
    }

    @Test
    public void toStringTest() {
        PastTask p = PastTask.createFrom(t);

        String expected = String.format("{ id: 0, name: Test Task, description: Scheduled @ %1$Tc; Task created by %5$s," +
                " creationTime: %2$Tc, startTime: %3$Tc, endTime: %4$Tc, result: FAILED, detailedResult: Exception at" +
                " 0.0000 %% because java.lang.Exception: Simulated fail", t.getScheduleTime(), t.getCreationTime(),
                t.getStartTime(), t.getEndTime(), t.getCreator());

        assertEquals(expected, p.toString().substring(0, expected.length()));
    }

    @Test
    public void periodicTaskTest() {
        t.setPeriod(60000);

        PastTask p = PastTask.createFrom(t);

        assertEquals(String.format("Periodic task starting @ %Tc repeating every %d milliseconds; Task created by %s",
                t.getScheduleTime(), t.getPeriod(), t.getCreator()), p.getDescription());
    }



}
