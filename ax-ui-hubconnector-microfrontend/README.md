# ADAMOS Hub Connector plugin

The Cumulocity IoT ADAMOS Hub Connector provides 2-way synchronization of devices between ADAMOS Hub Services and a Cumulocity IoT Tenant. The following functionality is provided:

* Synchronization between the ADAMOS Hub MDM Services and the Cumulocity IoT Inventory
* Fetching of product images from the ADAMOS Hub Catalog Service
* The Cumulocity IoT ADAMOS Hub Connector is based on version 1.0 of the ADAMOS Hub Services.

* Adds a menu point ADAMOS Hub
** Devices
** Events from Hub
** Settings

## Micro Frontend Plugin

This project was created using the widget-plugin template from c8ycli. This means you can build and deploy this project as plugin and add it's functionality to any existing app on the tenant. As the current plugin approach does not support internationalization, the package.json is still configured to build as standalone app and not as plugin. If you want to revert that, simply change
```
    "isPackage": false,
    "noAppSwitcher": false,
``` 
to
```
    "isPackage": true,
    "noAppSwitcher": true,
``` 

## API Documentation
The OpenAPI specification of the Connector API is listed here:
https://github.com/SoftwareAG/c8y-adamos-hub-connector/blob/main/hubconnector-oas.json

_________________

These tools are provided as-is and without warranty or support. They do not constitute part of the Software AG product suite. Users are free to use, fork and modify them, subject to the license agreement. While Software AG welcomes contributions, we cannot guarantee to include every contribution in the master project.
