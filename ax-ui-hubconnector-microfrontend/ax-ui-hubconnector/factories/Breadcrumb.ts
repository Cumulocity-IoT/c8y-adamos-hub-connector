import { Injectable } from "@angular/core";
import {
  BreadcrumbFactory,
  BreadcrumbItem,
  Breadcrumb,
  _,
} from "@c8y/ngx-components";
import { Router } from "@angular/router";

/**
 * A breadcrumb is a type of secondary navigation scheme that reveals the user’s location
 * in the application.
 */
@Injectable()
export class HubConnectorBreadcrumbFactory implements BreadcrumbFactory {
  // Inject the angular Router
  constructor(private router: Router) {}

  // Implement the get()-method, otherwise the ExampleBreadcrumbFactory
  // implements the BreadcrumbFactory interface incorrectly (!)
  get() {
    // Mandantory for a Breadcrumb is an array of BreadcrumbItem
    const breadcrumb: Breadcrumb = { items: [] };
    // Mandantory for a BreadcrumbItem is:
    //  - path (string)
    //  - label (string)
    const breadcrumbItems: BreadcrumbItem[] = [];
    if (this.router.url.match(/hub/g)) {
      if (this.router.url.match(/devices\/[0-9]*/g)) {
        breadcrumbItems.push({
          label: _("Devices"),
          path: "/hub/devices",
          icon: "c8y-icon c8y-icon-device",
        });
        // breadcrumbItems.push({
        //     label: this.router.url.substr(this.router.url.lastIndexOf("/") + 1),
        //     path: this.router.url
        // });
      }

      if (this.router.url.match(/mappings\/[0-9]*/g)) {
        breadcrumbItems.push({
          label: _("Mappings"),
          path: "/hub/mappings",
          icon: "c8y-icon c8y-icon-device-protocols",
        });
      }
    }

    breadcrumb.items = breadcrumbItems;
    return breadcrumb;
  }
}
