package net.lzzy.practicesonline.activities.network;

import net.lzzy.practicesonline.activities.models.Practice;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by lzzy_gxy on 2019/4/22.
 * Description:
 */
public class PracticeServiceTest {

    @Test
    public void testGetPracticeFromServer() throws IOException {
        String actual = PracticeService.getPracticeFromServer();
        assertTrue(actual.contains("测试阶段的划分"));
    }

    @Test
    public void testGetPratices() throws IOException, IllegalAccessException, JSONException, InstantiationException {
        String json = PracticeService.getPracticeFromServer();
        List<Practice> practices = PracticeService.getPratices(json);
        assertEquals(5, practices.size());
        assertEquals(33, practices.get(0).getApiId());
        assertEquals("测试阶段的划分", practices.get(0).getName());
        assertTrue(practices.get(0).getOutlines().contains("单元测试"));
        assertEquals(9, practices.get(0).getQuestionCount());
    }
}