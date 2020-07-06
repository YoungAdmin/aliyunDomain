package com.ovozz.domain;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.*;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.ovozz.domain.utils.GetPublicIP;
import com.ovozz.domain.utils.PropertiesUtil;

public class ApplicationStartup {

    //#阿里云给你的id和key
    private static String accessID = "LTAI4GDkXLQZAjtcTjdK9SJE";
    private static String accesskey = "EZxxujbgiLhbusGFNE5GhxmX40Ylh3";
    //域名名称
    private static String domainName = "ovozz.com";
    //主机记录。如果要解析@.exmaple.com，主机记录要填写”@”，而不是空。
    private static String RR = "domain";
    //获取外网ip服务器地址
    private static String getIpUrl = "http://myip.dnsomatic.com/";
    //执行周期 单位毫秒 10分钟/次
    private static Long executionCycleTimeLong = 1000 * 60 * 10l;


    public static void loadProperties(){
        accessID = PropertiesUtil.getPropery("AccessID");
        accesskey = PropertiesUtil.getPropery("Accesskey");
        domainName = PropertiesUtil.getPropery("DomainName");
        RR = PropertiesUtil.getPropery("RR");
        getIpUrl = PropertiesUtil.getPropery("getIpUrl");
        executionCycleTimeLong = Long.valueOf(PropertiesUtil.getPropery("executionCycleTimeLong"));
        System.out.println("conf.properties文件读取成功");
    }

    public static void main(String[] args) throws Exception {

        DefaultProfile profile = DefaultProfile.getProfile(
                "cn-hangzhou",
                accessID,
                accesskey);
        IAcsClient client = new DefaultAcsClient(profile);
        System.out.println("--阿里云API接口连接对象初始化成功--");
        //加载配置文件
        loadProperties();
        GetPublicIP.URL = getIpUrl;
        while (true){

            try{
                //获取本机公网ip
                String ip = null;
                try{
                    ip = GetPublicIP.getWebIp();
                }catch (Exception e){
                    System.out.println("主机名：" + GetPublicIP.getHostName());
                    System.out.println("内网ip：" + GetPublicIP.getIp());
                    System.out.println("本机ip获取失败,将在10秒后重试.......");
                    System.err.println("异常原因：" + e.getMessage());
                    Thread.sleep(1000 * 10);
                    continue;
                }
                System.out.print("主机名：" + GetPublicIP.getHostName());
                System.out.print("\t内网ip：" + GetPublicIP.getIp());
                System.out.println("\t公网ip：" + ip);

                //调用DescribeDomainRecords根据传入参数获取指定主域名的所有解析记录列表
                DescribeDomainRecordsResponse.Record record = null;
                DescribeDomainRecordsRequest queryRequest = new DescribeDomainRecordsRequest();
                queryRequest.setDomainName(domainName);
                DescribeDomainRecordsResponse queryResponse = client.getAcsResponse(queryRequest);
                //找到指定的主机记录
                for (DescribeDomainRecordsResponse.Record r :queryResponse.getDomainRecords()) {
                    if(r.getRR().equals(RR)){
                        System.out.println("查询指定解析记录成功！");
                        record = r;
                        break;
                    }
                }
                //修改该记录为当前主机外网ip
                UpdateDomainRecordRequest request = new UpdateDomainRecordRequest();
                request.setSysRegionId("cn-hangzhou");
                request.setRecordId(record.getRecordId());
                request.setRR(record.getRR());
                request.setType(record.getType());
                request.setValue(ip);

                if(request.getValue().equals(record.getValue())){
                    System.out.println("当前ip与阿里云解析ip一致，无需修改，1分钟后再次查询");
                    Thread.sleep(1000 * 60 * 1);
                    continue;
                }
                UpdateDomainRecordResponse response = client.getAcsResponse(request);
                System.out.println("修改解析成功 ip:" + request.getValue() + "recordId:" + response.getRecordId());
            } catch (ServerException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                System.err.println("ErrMsg:" + e.getErrMsg());
            }catch (Exception e){
                e.printStackTrace();
            }
            //10分钟获取获取一次当前主机的公网ip并使用阿里云的域名进行解析该ip
            Thread.sleep(executionCycleTimeLong);
        }
    }
}
