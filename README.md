# Weather Information API

## Overview

The **Weather Information API** provides real-time weather details for a given location (pin code) and date. This API allows users to retrieve weather data such as temperature, humidity, cloudiness, and wind speed for a specific date and location.

The data is fetched based on the **pin code** of the location and the **date** for which weather information is requested. It returns a detailed weather report with attributes like sunrise and sunset times, weather conditions, and various metrics related to the weather.

## Endpoints

### `GET /api/weather`

Fetches weather details for a given **pin code** and **date**.

#### Request Parameters

- `pinCode` (required): The pin code of the location for which weather information is requested.
- `forDate` (required): The date for which the weather data is required, formatted as `YYYY-MM-DD`.

#### Example Request

```
GET http://localhost:8080/api/weather?pinCode=721151&forDate=2024-11-09
```

#### Example Response

The API will respond with a detailed weather report in JSON format. Below is an example response:

```json
{
    "id": 1,
    "pinCode": "721151",
    "date": "2024-11-09",
    "country": "IN",
    "timezoneOffset": 19800,
    "sunriseTime": 1731111472,
    "sunsetTime": 1731151661,
    "location": {
        "id": 1,
        "pinCode": "721151",
        "latitude": 22.4586,
        "longitude": 87.7745,
        "cityName": "Tamlūk"
    },
    "weatherDetails": [
        {
            "id": 1,
            "main": "Clear",
            "description": "clear sky",
            "icon": "01d",
            "temp": 28.2,
            "feelsLike": 29.4,
            "humidity": 57,
            "pressure": 1015,
            "cloudiness": 0,
            "windSpeed": 3.31,
            "visibility": 10000
        }
    ]
}
```

#### Response Fields

- `id`: A unique identifier for the weather record.
- `pinCode`: The pin code of the requested location.
- `date`: The date for which the weather data is requested.
- `country`: The country code where the location is located.
- `timezoneOffset`: The timezone offset from UTC in seconds.
- `sunriseTime`: The sunrise time in Unix timestamp format.
- `sunsetTime`: The sunset time in Unix timestamp format.
- `location`: Contains information about the location, including:
    - `id`: Unique ID for the location.
    - `pinCode`: Pin code for the location.
    - `latitude`: Latitude of the location.
    - `longitude`: Longitude of the location.
    - `cityName`: The name of the city.
- `weatherDetails`: An array containing weather details, including:
    - `id`: Unique ID for the weather record.
    - `main`: Main weather condition (e.g., clear, rain).
    - `description`: Detailed description of the weather condition.
    - `icon`: Icon representing the weather condition.
    - `temp`: Current temperature in Celsius.
    - `feelsLike`: Temperature as it feels to the human body.
    - `humidity`: Humidity percentage.
    - `pressure`: Atmospheric pressure in hPa.
    - `cloudiness`: Cloud cover percentage.
    - `windSpeed`: Wind speed in m/s.
    - `visibility`: Visibility in meters.

## Requirements

- Spring Boot
- Java 21
- A valid service for fetching weather information (integration with external APIs for real-time data).

## Installation and Setup

1. Clone the repository:

    ```bash
    git clone https://github.com/your-username/weather-info-api.git
    ```

2. Navigate to the project directory:

    ```bash
    cd weather-info-api
    ```

3. Build the project using Maven:

    ```bash
    mvn clean install
    ```

4. Run the Spring Boot application:

    ```bash
    mvn spring-boot:run
    ```

5. The application will be available at `http://localhost:8080/api/weather`.

## Example Usage

To get the weather details for a location (pin code `721151`) on **2024-11-09**, make a GET request to the following URL:

```
http://localhost:8080/api/weather?pinCode=721151&forDate=2024-11-09
```

## Conclusion

This API provides essential weather data for any location by its pin code. It can be used for various applications such as weather forecasting, climate monitoring, and integrating weather data into other services.
```

### Key Features:
- The API accepts `pinCode` and `forDate` as query parameters.
- Provides detailed weather information including temperature, humidity, wind speed, etc.
- Easy to set up with Spring Boot and Java.

