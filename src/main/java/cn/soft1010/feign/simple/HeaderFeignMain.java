package cn.soft1010.feign.simple;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bjzhangjifu on 2018/8/24.
 */
public class HeaderFeignMain {

    public static void main(String[] args) {
        /**
         * 包含header  body
         */
        TestHeader testHeader = Feign.builder().target(TestHeader.class, "http://crm3.netease.com");
        Gson gson = new GsonBuilder().serializeNulls().create();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("page", 1);
        params.put("pageSize", 10);
        String body = gson.toJson(params);
        System.out.println(testHeader.test("qFnUFs5k__3NRAEjjqCK5Y9vCV4-i8H7XEYqIkqymb3zjeOjfQK47thg1C7DRpp1ifHt4vFoNr0E9oaQbJC3iw"));
        System.out.println(testHeader.test2("qFnUFs5k__3NRAEjjqCK5Y9vCV4-i8H7XEYqIkqymb3zjeOjfQK47thg1C7DRpp1ifHt4vFoNr0E9oaQbJC3iw"));
        System.out.println(testHeader.test3("qFnUFs5k__3NRAEjjqCK5Y9vCV4-i8H7XEYqIkqymb3zjeOjfQK47thg1C7DRpp1ifHt4vFoNr0E9oaQbJC3iw", body));

    }
}
