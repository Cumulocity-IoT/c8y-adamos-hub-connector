import { Injectable } from "@angular/core";
import { NavigatorNode, NavigatorNodeFactory, _ } from "@c8y/ngx-components";

@Injectable()
export class HubConnectorNavigationFactory implements NavigatorNodeFactory {
  get() {
    // nav.push(new NavigatorNode({
    //   label: _("Mappings"),
    //   icon: 'c8y-icon c8y-icon-device-protocols',
    //   path: "/mappings",
    //   routerLinkExact: false,
    //   priority: 99
    // }));

    // nav.push(new NavigatorNode({
    //   label: _('Hierarchy'),
    //   icon: 'c8y-icon fa fa-sitemap',
    //   path: '/hierarchy',
    //   routerLinkExact: true,
    //   priority: 99
    // }));

    const children: NavigatorNode[] = [
      new NavigatorNode({
        label: _("Devices"),
        icon: "c8y-icon c8y-icon-device",
        path: "/hub/devices",
        routerLinkExact: false,
        priority: 100,
      }),

      new NavigatorNode({
        label: _("Events from Hub"),
        icon: "c8y-icon c8y-icon-events",
        path: "/hub/eventRules",
        routerLinkExact: true,
        priority: 99,
      }),

      new NavigatorNode({
        label: _("Settings"),
        icon: "c8y-icon c8y-icon-administration",
        path: "/hub/settings",
        priority: 98,
      }),
    ];

    const parent = new NavigatorNode({
      label: _("ADAMOS Hub"),
    });
    parent.children = children;

    return [parent];
  }
}
