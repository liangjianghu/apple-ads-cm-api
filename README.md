# apple_search_ads_api

官方 API 文档: https://developer.apple.com/documentation/apple_search_ads

PDF 格式（2020 年 5 月更新）: https://github.com/liangjianghu/apple_search_ads_api/blob/main/Apple-Search-Ads-API-2.0_May_2020_release.pdf


## Overview
With the [Apple Search Ads Campaign Management API](https://developer.apple.com/documentation/apple_search_ads), you can build campaigns containing total budgets and daily caps, and create ad groups containing keywords, Creative Sets, audience refinement criteria, and scheduling. You can implement your own keyword-bidding strategy or use the Search Match feature to automatically match your ad to relevant user searches on App Store Connect. When you are ready, promote your campaigns to multiple countries or regions and generate reports with meaningful metrics.


## Quick Start

### Generate an API Certificate

* [广告主自行生成和下载证书说明](https://developer.apple.com/documentation/apple_search_ads/authenticating_with_the_apple_search_ads_api)
* 委托量江湖投放的广告主，请联系对接商务或优化师提供 API 证书

### Create a PKCS#12 File

[Windows 系统安装 openssl](https://slproweb.com/products/Win32OpenSSL.html)

```bash
openssl pkcs12 -export -in <PEM_file>.pem -inkey <PRIVATE_KEY>.key -out <FILENAME>.p12

```

### 特别提醒

```
- 目前 Search Ads 官方尚未提供各语言的 SDK，需开发者自行实现
- Get User ACL 接口用于获取证书对应的 orgIds
- 下文中出现的 9999999 或 1234567890 都是随机数字，需替换为实际数值
- 其他所有接口都必须基于一个orgId范围内，即 Header 必须包括 Authorization: orgId=9999999
```

#### 403 错误常见原因

```diff
- 接口请求如果返回如下错误信息，通常是因为 Header 未正确设置 (Authorization: orgId=)
- 请指定正确的orgId参数，该参数可由 User ACL接口自行获取，或联系量江湖获得
```

```
{
  "data": null,
  "pagination": null,
  "error": {
    "errors": [
      {
        "messageCode": "FORBIDDEN",
        "message": "User does not have required permissions",
        "field": ""
      }
    ]
  }
}
```


### Get User ACL

#### Request

```bash
curl -v -X GET \
 https://api.searchads.apple.com/api/v3/acls \
 --cert-type p12 \
 --cert <FILENAME>.p12 \
 --pass <PASSWORD>
```

#### Response

```json
{
  "data": [
    {
      "orgName": "Organization Name (Campaign Group Name)",
      "orgId": 9999999,
      "currency": "USD",
      "timeZone": "America/Los_Angeles",
      "paymentModel": "LOC",
      "roleNames": [
        "Read Only"
      ],
      "certExpirationDate": null
    }
  ],
  "pagination": null,
  "error": null
}
```

### Get all Campaigns

#### Request

```bash
curl -v -X GET \
 https://api.searchads.apple.com/api/v3/campaigns \
 -H 'Authorization: orgId=9999999' \
 -H 'Content-Type: application/json' \
 --cert-type p12 \
 --cert <FILENAME>.p12 \
 --pass <PASSWORD>
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
curl -v -X GET \
 https://api.searchads.apple.com/api/v3/campaigns?fields=id,name,countriesOrRegions \
 -H 'Authorization: orgId=9999999' \
 -H 'Content-Type: application/json' \
 --cert-type p12 \
 --cert <FILENAME>.p12 \
 --pass <PASSWORD>
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
curl -v -X POST \
 https://api.searchads.apple.com/api/v3/reports/campaigns \
 -H 'Authorization: orgId=9999999' \
 -H 'Content-Type: application/json' \
 --cert-type p12 \
 --cert <FILENAME>.p12 \
 --pass <PASSWORD>
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
