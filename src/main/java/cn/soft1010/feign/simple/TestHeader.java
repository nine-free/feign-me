package cn.soft1010.feign.simple;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * Created by bjzhangjifu on 2018/8/24.
 */
@Headers({"Content-Type: application/json;charset=UTF-8", "TOKEN: {authToken}"})
public interface TestHeader {
    @RequestLine("GET /route/crm-api/user/contacts/getAllDepList")
    String test(@Param("authToken") String token);

    @RequestLine("POST /route/crm-api/approval/rest/approval/cost/listApprovalStates")
    String test2(@Param("authToken") String token);

    @RequestLine("POST /route/crm-api/other/rest/costItems/queryCostItemList.do")
    @Body("{jsonBody}")
    String test3(@Param("authToken") String token, @Param("jsonBody") String jsonBody);
}
