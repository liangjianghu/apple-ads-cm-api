# 概要

本文档主要阅读对象为产品人员和技术工程师。

Apple Search Ads Campaign Management API （下面简称API）是以程序化方式管理 ASA 广告系列并生成数据报告的 API。在实践中，广告主通常借助此 API 获取数据报告，整合至内部的 BI 系统，或者三方归因平台抓取广告花费数据。量江湖基于此 API 构建了用于管理广告系列、为广告主实现广告 ROI 最大化的智能投放系统。

官方 API 文档: https://developer.apple.com/documentation/apple_search_ads

最新版本为 V4：https://developer.apple.com/documentation/apple_search_ads/apple_search_ads_campaign_management_api_4

关于主要的 OAuth 认证部分，可参考下列项目：

1.  [Java 代码](https://github.com/liangjianghu/apple-ads-java-demo)

2.  [PHP 代码](https://github.com/liangjianghu/apple-ads-php-demo)

3.  [Python 代码](https://github.com/liangjianghu/apple-ads-python-demo)

# 联系量江湖

如有疑问，可联系量江湖技术支持，扫描下方二维码（左微信、右钉钉）

<img src="https://user-images.githubusercontent.com/231417/110305605-d5980b00-8037-11eb-9aca-5cf425a55337.png" width="200" /> <img src="https://user-images.githubusercontent.com/231417/119437724-4c989200-bd51-11eb-9316-fa5c15ffd6f3.png" width="200" />


# 使用三方MMP来获取花费数据

1.	此功能首先需要MMP 的产品升级支持；量江湖了解到各个MMP正在升级其花费功能模块，具体进度需以其官方宣布为准。

2.	其中根据AppsFlyer后台已上线的版本，经测试，此项操作授权必需由量江湖完成，无需广告主操作。但其还未正式发布，需再观察进度。

# 开发者自行开发

开发者可通过此 API 获取花费报表、账户层级id-name对照等数据，将数据整合到 app 的后台系统中。

## 实施 OAuth 认证，获取 access_token

1.	联系量江湖（或者您的账户管理员），邀请您使用 ASA 平台，授予权限

2.	您将接收到包含链接和验证码的邀请邮件，点击链接并输入验证码，登录苹果 ASA 后台，完成接受邀请

3.	生成密钥对

```shell
# 生成私钥，下面第5步需用到
openssl ecparam -genkey -name prime256v1 -noout -out YOUR-PRIVATE-KEY.pem

# 生成公钥
openssl ec -in YOUR-PRIVATE-KEY.pem -pubout -out YOUR-PUBLIC-KEY.pem
```

4.	将公钥（YOUR-PUBLIC-KEY.pem文件内容）粘贴到 ASA 后台（设置 > API），生成 clientId, teamId, keyId

5.	结合如上三个Id，实现一段代码，使用私钥签名生成 client secret，有效期最长可设置为 180 天，以下是 Python 代码示例：

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
KEY_FILE = 'YOUR-PRIVATE-KEY.pem' 

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

5.	通过 client secret 从鉴权服务器获得访问令牌 **access_token**，有效期为 1 个小时

```shell
curl -X POST "https://appleid.apple.com/auth/oauth2/token" \
-H 'Content-Type: application/x-www-form-urlencoded' \
-d 'grant_type=client_credentials&client_id={client_id}&client_secret={client_secret}&scope=searchadsorg'
```

```python
import requests
url = "https://appleid.apple.com/auth/oauth2/token"
headers = {"Content-Type": "application/x-www-form-urlencoded"}
data = { "grant_type": "client_credentials", "client_id": "XXXclient_id", "client_secret": "YYYclient_secret", "scope": "searchadsorg"}
r = requests.post(url, headers=headers, data=data)
print(r.json())
```

6.	在请求广告管理 API 的 Header Authorization 参数中将访问令牌 access_token 作为 Bearer 传递，请求示例如下：

```shell
curl "https://api.searchads.apple.com/api/v4/campaigns"
-H "Authorization: Bearer {access_token}" \
-H "X-AP-Context: orgId={orgId}"
```

## 接口请求示例

### 访问接口 Get User ACL，获取 orgIds

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
    "startTime": "2021-06-14",
    "endTime": "2021-06-16",
    "granularity": "DAILY",
    "selector": {
        "orderBy": [
            {
                "field": "localSpend",
                "sortOrder": "DESCENDING"
            }
        ],
        "pagination": {
            "offset": 0,
            "limit": 1000
        }
    },
    "timeZone": "ORTZ",
    "returnRecordsWithNoMetrics": false,
    "returnRowTotals": false,
    "returnGrandTotals": false
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

### 关于 V4 版本更新变化

对于实施过 V3 版本的开发者，以下几个是 Apple Search Ads API 4.0 版本的主要变化：

#### 1、认证过程启用 OAuth 2.0
 
#### 2、访问控制列表 (ACL) 将新增一个上级组织字段

  <img src="https://user-images.githubusercontent.com/231417/119435561-f1fd3700-bd4c-11eb-91fe-9368e4e5d481.png" width="300" />

#### 3、广告账户结构及报表元数据新增字段

  <img src="https://user-images.githubusercontent.com/231417/119435577-f9244500-bd4c-11eb-8158-ea8176994e50.png" width="600" />

另外，报表中还会增加 avgCPM 指标，其计算方式为：(spend*1000)/impressions

新增字段主要应对刚发布的搜索标签广告系列，以及将来可能发布的更多展示类广告。
