# What is it?

This is a project for Java intensive course.

# How to run

Application using minikube, so you can launch program with next commands:

```
minikube start --addons ingress; kubectl apply -f kubernetes
```

# Example request:

Get all apartments:

```
curl --resolve "hotel-service.com:80:127.0.0.1" http://hotel-service.com/apartments?sort=id
```

Add an apartment:

```
curl --resolve "hotel-service.com:80:127.0.0.1" -X POST  http://hotel-service.com/apartments -H "Content-Type:application/json" -d '{"id":"00000000-0000-0000-0000-000000000003","price":3,"capacity":1,"availability":true,"status":"AVAILABLE"}'
```

# Application Endpoints

This application provides various endpoints to manage clients and apartments. Below is a list of them and descriptions:

## Client Endpoints

### Create Client

```
[POST] /clients
```
Request body **must** include:
```
{
    "id":"",
    "name":"",
    "status":""
}
```

Creates a *new client* with the **specified** data.


### Delete Client

```
[DELETE] /clients/{id}
```

*Deletes* the client with the **specified** UUID.

### Get Client

```
[GET] /clients/{id}
```

Retrieves and displays the *details of the client* with the **specified** UUID.

### Get All Clients

```
[GET] /clients?sort={sort}
```

***'sort'*** param have to contain [ ID NAME STATUS ]

Retrieves and displays *list of all clients* with **specified** sort param.

### Check-in Client

```
[PUT] /clients/check-in
```
Request body **must** include:
```
{
    "clientId":"",
    "apartmentId":""
}
```
Associates the client with the specified UUID with the apartment with the specified UUID.

### Check-out Client

```
[PUT] /clients/check-out
```
Request body **must** include:
```
{
    "clientId":"",
    "apartmentId":""
}
```
Disassociates the client with the specified UUID from the apartment with the specified UUID.

### Calculate Client Stay Price

```
[GET] /clients/stay?clientId={id}
```

Calculates and displays the price for the stay of the client with the specified UUID.

## Apartment Endpoints

### Create Apartment

```
[POST] /apartments
```

Request body **must** include:
```
{
    "id":"",
    "price":"",
    "capacity":"",
    "availability":"",
    "status":""
}
```
Creates a *new apartment* with the **specified** UUID, price, capacity, availability, and apartment status.

### Delete Apartment

```
[DELETE] /apartments/{id}
```

*Deletes* the apartment with the **specified** UUID.

### Get Apartment

```
[GET] /apartments/{id}
```

Retrieves and displays the *details of the apartment* with the **specified** UUID.

### Get All Apartments

```
[GET] /apartments/sort={sort}
```

***'sort'*** param have to contain [ ID CAPACITY AVAILABILITY PRICE ]

Retrieves and displays *list of all apartments* with **specified** sort param.

### Adjust Apartment Price

```
[PUT] /apartments/adjust
```

Request body **must** include:
```
{
    "clientId":"",
    "newPrice":""
}
```

*Adjusts the price* of the apartment with the **specified** UUID to the specified price.


