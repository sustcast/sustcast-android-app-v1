package com.sust.sustcast.utils;

import com.sust.sustcast.data.IceUrl;

import java.util.List;

public class LoadBalancingUtil {

    public static IceUrl selectIceCastSource(List<IceUrl> iceUrlList){
        IceUrl bestIceUrl = new IceUrl();

        float minLoad = Float.MAX_VALUE;

        for(IceUrl iceUrl : iceUrlList){
            int limit = iceUrl.getLimit();
            String url = iceUrl.getUrl();
            int numList = iceUrl.getNumlisteners();

            float load = (float) numList / (float) limit;

            if (load < 1 && load < minLoad) {
                minLoad = load;
                bestIceUrl = iceUrl;
            }
        }

        return bestIceUrl;
    }
}
