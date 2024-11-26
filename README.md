# Logifuture Wallet Challenge

## Description

A Spring Boot application providing an API to create, retrieve and update wallets
for a user using Redis as a data source.

## Requirements

Docker is required to run this application.

## How to use

### Starting the App

Within the root directory of the project, run `docker-compose up`. This will containerize
and run a Redis image as well as the application. Once the app successfully loads,
HTTP requests can be made.

### Authentication

An API key is required for all requests. It is configured as an environment variable
in `docker-compose.yml`. In each request, provide the key within the header `X-API-KEY`.

Note that in a production setting, this key would be externalized.

### Endpoints

The host is localhost on port 8081. There are three endpoints:

#### Wallet Creation

`POST localhost:8081/api/wallet` will create a wallet. The request body is a `Wallet` 
JSON object, which is structured like this example:

```JSON
{
    "id": "fec4f61c-8162-4cfd-88ce-146d57b50d7b",
    "balance": 100.00
}
```

It consists of a UUID serving as the wallet's unique key, as well as an initial balance.
The balance cannot be less than 0.

This endpoint requires an idempotency key in the headers to prevent simultaneous requests
from conflicting with another, and ensures requests perform a unique action. The idempotency
key is a UUID and each new request should use a newly generated UUID. Reusing a UUID within
24 hours of the original UUID will return the response which the previous request using that
UUID received. In each POST request, provide the idempotency key within the `Idempotency-Key`
header.

A successful response will return an HTTP 201 code and the `Wallet` object.

#### Wallet Fetching

`GET localhost:8081/api/wallet/{id}` will retrieve the `Wallet` provided in the path UUID
parameter if it exists.

As this request is inherently idempotent, no idempotency key is required.

#### Adding and Removing Wallet Funds

`PATCH localhost:8081/api/wallet/{id}?isAddingFunds=true&amount=100.00` will add funds
to the wallet provided in the path parameter, in this case $100.00 will be added.

`PATCH localhost:8081/api/wallet/{id}?isAddingFunds=false&amount=50.00` will remove funds
from the wallet provided in the path parameter, in this case $50.00 will be removed. Note
that an error will be returned if the provided amount is greater than the wallet's current
balance.

`isAddingFunds` is a required boolean query parameter indicating whether it is adding
(true) or removing (false) funds.

`amount` is a required decimal query parameter indicating the amount to add or remove.
This value must be greater than 0.

As the server is handling the addition and subtraction between the provided amount and the
wallet's current balance, the `PATCH` only updates the `balance` field in the `Wallet` 
object to the resulting balance, and returns the updated `Wallet` in the response body.
As this action is not idempotent, an idempotency key is required in the headers.

## Acknowledgments

This application uses an API key for authentication. A production application would have
the API key be generated per-user and use an encrypted secret. The API key is configured
within `docker-compose.yml` but this would be externalized in a production environment.

A production environment would also use authorization using a username and password along
with something like a JWT that ensures the user is authorized to call the endpoints. However,
we are not concerned with saving user data or credentials in this challenge.

Redis would also use credentials and have the container be hosted in a VPC in a production
setting.
