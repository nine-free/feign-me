

## feign 源码解析

**源码地址**


[feign github 托管地址](https://github.com/OpenFeign/feign)


**学习代码**


[学习实例](https://github.com/nine-free/feign-me)

###  feign 是什么
```
通过注解注入一个模板化请求进行工作java http 客户端

```

### feign 使用
maven依赖
```
<dependency>
    <groupId>com.netflix.feign</groupId>
    <artifactId>feign-core</artifactId>
    <version>8.18.0</version>
    <scope>runtime</scope>
</dependency>

```
1. 基本用法  (无参数访问百度首页)
```
BAIDUAPI api = Feign.builder().target(BAIDUAPI.class,"https://www.baidu.com");
String result = api.index();

```
```
    interface BAIDUAPI {
        @RequestLine("GET /")
        String index();
    }
```

2. 带参数的get请求
```
SougouAPI sougouAPI = Feign.builder().target(SougouAPI.class,"http://weixin.sogou.com/sugg/ajaj_json.jsp");
String result = sougouAPI.sougou("feign", System.currentTimeMillis());
```
```
interface SougouAPI {
        //        http://weixin.sogou.com/sugg/ajaj_json.jsp?key=feign&type=wxart&pr=web&t=1535020058746
        @RequestLine("GET ?key={key}&type=wxart&pr=web&t={timestamp}")
        String sougou(@Param(value = "key") String key, @Param(value = "timestamp") long timestamp);
    }
```
3. 请求header && 请求json消息体
```
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

```
```
@Headers({"Content-Type: application/json;charset=UTF-8", "TOKEN: {authToken}"})
    interface TestHeader {

        @RequestLine("GET /route/crm-api/user/contacts/getAllDepList")
        String test(@Param("authToken") String token);

        @RequestLine("POST /route/crm-api/approval/rest/approval/cost/listApprovalStates")
        String test2(@Param("authToken") String token);

        @RequestLine("POST /route/crm-api/other/rest/costItems/queryCostItemList.do")
        @Body("{jsonBody}")
        String test3(@Param("authToken") String token, @Param("jsonBody") String jsonBody);
    }
```

4. 复杂用法 拦截器 编码 解码 options(超时时间) 重试 日志
```
List<RequestInterceptor> requestInterceptors = new ArrayList<RequestInterceptor>();
        requestInterceptors.add(new MyRequestInterceptor());
        TestHeader testHeader2 = Feign.builder().
                requestInterceptors(requestInterceptors).
                decoder(new MyDecoder()).
                encoder(new MyEncoder()).
                options(new Request.Options(10000, 60000)).
                retryer(new MyRetryer(1000, 10000, 3)).
                logger(new MyLogger()).
                logLevel(Logger.Level.BASIC).
                target(TestHeader.class, "http://crm3.netease.com");
        System.out.println(testHeader2.test3("qFnUFs5k__3NRAEjjqCK5Y9vCV4-i8H7XEYqIkqymb3zjeOjfQK47thg1C7DRpp1ifHt4vFoNr0E9oaQbJC3iw", body));

```
**RequestInterceptor 拦截器**
```
    static class MyRequestInterceptor implements RequestInterceptor {
        @Override
        public void apply(RequestTemplate template) {
            System.out.println("MyRequestInterceptor  " + System.currentTimeMillis() + " 发起请求" + template.url() + " " + new String(template.body()));
        }
    }
```

**encoder**
```
   static class MyEncoder extends Encoder.Default {
        public void encode(Object o, Type type, RequestTemplate requestTemplate) throws EncodeException {
            System.out.println("MyEncoder " + type.toString() + "  " + o.toString());
        }
    }

```
**decoder**
```
  static class MyDecoder extends Decoder.Default {
        public Object decode(Response response, Type type) throws IOException, FeignException {
            if (response.status() == 200) {
                System.out.println("MyDecoder 请求成功");
            }
            return super.decode(response, type);
        }
    }
```
**logger**
```
static class MyLogger extends Logger {
        @Override
        protected void log(String configKey, String format, Object... args) {
            System.out.println(String.format("MyLogger " + methodTag(configKey) + format + "%n", args));
        }
        @Override
        protected void logRequest(String configKey, Level logLevel, Request request) {
            if (logLevel.equals(Level.BASIC)) {
                super.logRequest(configKey, logLevel, request);
            }
        }
    }
```
**retryer 重试机制**
```
static class MyRetryer implements Retryer {

        int maxAttempts;
        long period;
        long maxPeriod;
        int attempt;
        long sleptForMillis;

        public MyRetryer(long period, long maxPeriod, int maxAttempts) {
            this.period = period;
            this.maxAttempts = maxAttempts;
            this.maxPeriod = maxPeriod;
        }

        protected long currentTimeMillis() {
            return System.currentTimeMillis();
        }

        public void continueOrPropagate(RetryableException e) {
            if (this.attempt++ >= this.maxAttempts) {
                throw e;
            } else {
                long interval;
                if (e.retryAfter() != null) {
                    interval = e.retryAfter().getTime() - this.currentTimeMillis();
                    if (interval > this.maxPeriod) {
                        interval = this.maxPeriod;
                    }

                    if (interval < 0L) {
                        return;
                    }
                } else {
                    interval = this.nextMaxInterval();
                }

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException var5) {
                    Thread.currentThread().interrupt();
                }

                this.sleptForMillis += interval;
            }
        }

        long nextMaxInterval() {
            long interval = (long) ((double) this.period * Math.pow(1.5D, (double) (this.attempt - 1)));
            return interval > this.maxPeriod ? this.maxPeriod : interval;
        }

        public Retryer clone() {
            System.out.println("MyRetryer clone");
            return new MyRetryer(this.period, this.maxPeriod, this.maxAttempts);
        }
    }
```


注意：不同feign版本可能稍有不同 比如options构造方法 最新版本新增一个redirect参数



###  feign 主要源码解析

feign主要是将自己书写的带有注解的接口使用动态代理实例化之后发起http请求

 1、Feign.builder() 构建一个feign对象（包含多个组件 decoder encoder client retryer等）
 2、调用target方法 通过动态代理实例化一个接口类型的对象（其中target一个参数为接口type）
 3、使用实例化的接口对象调用具体方法
 
 **主要注解**
 ```
 @RequestLine
 @Headers
 @Body
 @Param
 ...
 
 ```
**主要类**
```
Feign 入口
ReflectiveFeign
ReflectiveFeign.FeignInvocationHandler  放射实现

```
![image](https://note.youdao.com/yws/api/personal/file/0DE9A6C9B62B469EBBFA8BC0E37B5153?method=download&shareKey=8b26b728d92dc36b63bc28afcdaeeca9)












