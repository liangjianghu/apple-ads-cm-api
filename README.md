# apple_search_ads_api


## Overview
With the [Apple Search Ads Campaign Management API](https://developer.apple.com/documentation/apple_search_ads), you can build campaigns containing total budgets and daily caps, and create ad groups containing keywords, Creative Sets, audience refinement criteria, and scheduling. You can implement your own keyword-bidding strategy or use the Search Match feature to automatically match your ad to relevant user searches on App Store Connect. When you are ready, promote your campaigns to multiple countries or regions and generate reports with meaningful metrics.


## Quick Start

### Generate an API Certificate

[how to](https://developer.apple.com/documentation/apple_search_ads/authenticating_with_the_apple_search_ads_api)

### Create a PKCS#12 File

```bash
openssl pkcs12 -export -in <PEM_file>.pem -inkey <PRIVATE_KEY>.key -out <FILENAME>.p12

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
