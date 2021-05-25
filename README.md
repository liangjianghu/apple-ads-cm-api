# apple_search_ads_api

官方 API 文档: https://developer.apple.com/documentation/apple_search_ads

V4 版本更新：https://developer.apple.com/documentation/apple_search_ads/apple_search_ads_campaign_management_api_4

联系量江湖技术支持，可微信扫描下方二维码

<img src="https://user-images.githubusercontent.com/231417/110305605-d5980b00-8037-11eb-9aca-5cf425a55337.png" width="200" />


## Overview

本文档主要阅读对象为产品人员和技术工程师。

Apple Search Ads Campaign Management API （下面简称API）是以程序化方式管理 ASA 广告系列并生成数据报告的 API。在实践中，广告主通常借助此 API 获取数据报告，整合至内部的 BI 系统，或者三方归因平台抓取广告花费数据。量江湖基于此 API 构建了用于管理广告系列、为广告主实现广告 ROI 最大化的智能投放系统。

Apple Search Ads 已发布广告系列管理 API 4.0 版。新版本的具体变化如下：

#### 1、认证过程需要启用 OAuth 2.0

  OAuth 实施介绍请看第二部分
 
#### 2、访问控制列表 (ACL) 将新增一个上级组织字段

  <img src="https://user-images.githubusercontent.com/231417/119435561-f1fd3700-bd4c-11eb-91fe-9368e4e5d481.png" width="300" />

#### 3、广告账户结构及报表元数据新增字段

  <img src="https://user-images.githubusercontent.com/231417/119435577-f9244500-bd4c-11eb-8158-ea8176994e50.png" width="600" />

另外，报表中还会增加 avgCPM 指标，其计算方式为：(spend*1000)/impressions

新增字段主要应对刚发布的搜索标签广告系列，以及将来可能发布的更多展示类广告。


## OAuth 认证步骤


其中，认证过程启用 OAuth 2.0 是比较大的变化，也是直接影响到开发者（广告主）或 MMP 获取花费报表的变化，以下是大致步骤：

### 一）使用三方MMP来获取花费数据

1.	此功能首先需要MMP 的产品升级支持；量江湖了解到各个MMP正在升级其花费功能模块，具体进度需以其官方宣布为准。

2.	其中根据AppsFlyer后台已上线的版本，经测试，此项操作授权必需由量江湖完成，无需广告主操作。但其还未正式发布，需再观察进度。

### 二）开发者自行获取花费等报表数据

1.	由苹果 ASA 广告账户的管理员用户（联系您的代理服务商），邀请一个用户（需较稳定的 Apple ID）授予对应子账户的【API 只读】权限

2.	用户将接收到包含链接和验证码的邀请邮件，用户点击链接并输入验证码，登录苹果 ASA 后台，完成接受邀请

3.	开发者在本地生成密钥对

```shell
# 生成私钥，下面第5步需用到
openssl ecparam -genkey -name prime256v1 -noout -out YOUR-PRIVATE-KEY.pem

# 生成公钥
openssl ec -in YOUR-PRIVATE-KEY.pem -pubout -out YOUR-PUBLIC-KEY.pem
```

4.	将公钥（YOUR-PUBLIC-KEY.pem文件内容）粘贴到 ASA 后台（设置 > API），生成 clientId, teamId, keyId

5.	结合如上三个Id，开发者需实现一段代码，使用私钥签名生成 client secret，有效期最长可设置为 180 天

```python
import jwt
import datetime as dt

client_id = 'SEARCHADS.27478e71-3bb0-4588-998c-182e2b405577'
team_id = 'SEARCHADS.27478e71-3bb0-4588-998c-182e2b405577' 
key_id = 'bacaebda-e219-41ee-a907-e2c25b24d1b2' 
audience = 'https://appleid.apple.com'
alg = 'ES256'

# Define issue timestamp.
issued_at_timestamp = int(dt.datetime.utcnow().timestamp())
# Define expiration timestamp. May not exceed 180 days from issue timestamp.
expiration_timestamp = issued_at_timestamp + 86400*180 

# Define JWT headers.
headers = dict()
headers['alg'] = alg
headers['kid'] = key_id

# Define JWT payload.
payload = dict()
payload['sub'] = client_id
payload['aud'] = audience
payload['iat'] = issued_at_timestamp
payload['exp'] = expiration_timestamp
payload['iss'] = team_id 

# Path to signed private key.
KEY_FILE = 'private-key.pem' 

with open(KEY_FILE,'r') as key_file:
     key = ''.join(key_file.readlines())

client_secret = jwt.encode(
payload=payload,  
headers=headers,
algorithm=alg,  
key=key
)

with open('client_secret.txt', 'w') as output: 
     output.write(client_secret.decode("utf-8"))
```

5.	通过 client secret 从鉴权服务器获得访问令牌 access_token，有效期为 1 个小时

```shell
curl -X POST "https://appleid.apple.com/auth/oauth2/token" \
-H 'Content-Type: application/x-www-form-urlencoded' \
-d 'grant_type=client_credentials&client_id={client_id}&client_secret={client_secret}&scope=searchadsorg'
```

6.	在请求广告管理 API 的 Header Authorization 参数中将访问令牌 access_token 作为 Bearer 传递，请求示例如下：

```shell
curl "https://api.searchads.apple.com/api/v4/campaigns"
-H "Authorization: Bearer {access_token}" \
-H "X-AP-Context: orgId={orgId}"
```

## 接口请求示例

### Get User ACL

#### Request

```bash
curl -v -X GET https://api.searchads.apple.com/api/v4/acls \
-H "Authorization: Bearer {access_token}"
```

#### Response

```json
{
  "data": [
    {
      "orgName": "OrgNameXXX",
      "orgId": 1234678,
      "currency": "USD",
      "timeZone": "America/Los_Angeles",
      "paymentModel": "LOC",
      "roleNames": [
        "API Read Only"
      ],
      "parentOrgId": 100123
    },
    {
      "orgName": "OrgNameYYY",
      "orgId": 1234789,
      "currency": "USD",
      "timeZone": "America/Los_Angeles",
      "paymentModel": "LOC",
      "roleNames": [
        "API Read Only"
      ],
      "parentOrgId": 100123
    }
  ],
  "pagination": null,
  "error": null
}
```

### Get all Campaigns

#### Request

```bash
curl -v -X GET "https://api.searchads.apple.com/api/v4/campaigns" \
 -H "Authorization: Bearer {access_token}" \
 -H 'X-AP-Context: orgId={orgId}' \
 -H 'Content-Type: application/json'
```

#### Response

```json
{
  "data": [
    {
      "id": 1234567890,
      "orgId": 1234567890,
      "name": "Campaign Name",
      "budgetAmount": {
        "amount": "20000000",
        "currency": "USD"
      },
      "dailyBudgetAmount": {
        "amount": "400",
        "currency": "USD"
      },
      "adamId": 1234567890,
      # ....
    }
    # ...
  ],
  "pagination": {
    "totalResults": 2,
    "startIndex": 0,
    "itemsPerPage": 2
  },
  "error": null
}
```

### Get all Campaigns (返回部分属性)

#### Request

```bash
curl -v -X GET "https://api.searchads.apple.com/api/v4/campaigns?fields=id,name,countriesOrRegions" \
 -H "Authorization: Bearer {access_token}" \
 -H 'X-AP-Context: orgId={orgId}' \
 -H 'Content-Type: application/json'
```

#### Response

```json
{
  "data": [
    {
      "id": 123456789011,
      "name": "us-campaign-name",
      "countriesOrRegions": [
        "US"
      ]
    },
    {
      "id": 123456789022,
      "name": "il-campaign-name",
      "countriesOrRegions": [
        "IL"
      ]
    }
  ],
  "pagination": {
    "totalResults": 2,
    "startIndex": 0,
    "itemsPerPage": 2
  },
  "error": null
}
```

### Get Campaign Level Reports

#### Request

```diff
- 报表接口 URL 包括 /reports/ 
- 报表接口使用 POST 请求方法
```

```bash
curl -v -X POST "https://api.searchads.apple.com/api/v4/reports/campaigns" \
 -H "Authorization: Bearer {access_token}" \
 -H 'X-AP-Context: orgId={orgId}' \
 -H 'Content-Type: application/json' \
 -d '{
    "startTime": "2020-11-01",
    "endTime": "2020-11-14",
    "selector": {
        "orderBy": [
            {
                "field": "countryOrRegion",
                "sortOrder": "ASCENDING"
            }
        ],
        "conditions": [
            {
                "field": "countriesOrRegions",
                "operator": "CONTAINS_ANY",
                "values": [
                    "US"
                ]
            }
        ],
        "pagination": {
            "offset": 0,
            "limit": 1000
        }
    },
    "groupBy": [
        "countryOrRegion"
    ],
    "timeZone": "UTC",
    "returnRecordsWithNoMetrics": true,
    "returnRowTotals": true,
    "returnGrandTotals": true
}'
```

#### Response

```json
{
  "data": {
    "reportingDataResponse": {
      "row": [
        {
          "other": false,
          "total": {
            "impressions": 123456,
            "taps": 123456,
            "installs": 123456,
            "newDownloads": 123456,
            "redownloads": 123456,
            "latOnInstalls": 123456,
            "latOffInstalls": 123456,
            "ttr": 0.123456,
            "avgCPA": {
              "amount": "1.23456",
              "currency": "USD"
            },
            "avgCPT": {
              "amount": "1.23456",
              "currency": "USD"
            },
            "localSpend": {
              "amount": "1234.56",
              "currency": "USD"
            },
            "conversionRate": 0.123456
          },
          "metadata": {
            "campaignId": 1234567890,
            "campaignName": "campaignName",
            "deleted": false,
            "campaignStatus": "ENABLED",
            "app": {
              "appName": "appName",
              "adamId": 1234567890
            },
            "servingStatus": "RUNNING",
            "servingStateReasons": null,
            "countriesOrRegions": [
              "US"
            ],
            "modificationTime": "2020-11-13T08:47:36.054",
            "totalBudget": {
              "amount": "20000000",
              "currency": "USD"
            },
            "dailyBudget": {
              "amount": "8000",
              "currency": "USD"
            },
            "displayStatus": "RUNNING",
            "supplySources": [
              "APPSTORE_SEARCH_RESULTS"
            ],
            "adChannelType": "SEARCH",
            "orgId": 1234567890,
            "countryOrRegionServingStateReasons": {},
            "countryOrRegion": "US"
          }
        }
      ],
      "grandTotals": {
        "other": false,
        "total": {
            "impressions": 123456,
            "taps": 123456,
            "installs": 123456,
            "newDownloads": 123456,
            "redownloads": 123456,
            "latOnInstalls": 123456,
            "latOffInstalls": 123456,
            "ttr": 0.123456,
            "avgCPA": {
              "amount": "1.23456",
              "currency": "USD"
            },
            "avgCPT": {
              "amount": "1.23456",
              "currency": "USD"
            },
            "localSpend": {
              "amount": "1234.56",
              "currency": "USD"
            },
            "conversionRate": 0.123456
        }
      }
    }
  },
  "pagination": {
    "totalResults": 2,
    "startIndex": 0,
    "itemsPerPage": 2
  },
  "error": null
}
```
