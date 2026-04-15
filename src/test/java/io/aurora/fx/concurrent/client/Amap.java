//package io.data.chain.fx.concurrent.client;
//
//import com.dtflys.forest.annotation.BaseRequest;
//import com.dtflys.forest.annotation.Get;
//import com.dtflys.forest.annotation.Var;
//import io.data.chain.fx.concurrent.model.Location;
//import io.data.chain.fx.concurrent.model.Result;
//
//@BaseRequest(baseURL = "http://ditu.amap.com")
//public interface Amap {
//
//    /**
//     * 根据经纬度获取详细地址
//     * @param longitude 经度
//     * @param latitude 纬度
//     * @return
//     */
//    @Get("http://ditu.amap.com/service/regeo?longitude={lng}&latitude={lat}")
//    Result<Location> getLocation(@Var("lng") String longitude, @Var("lat") String latitude);
//
//
//}
