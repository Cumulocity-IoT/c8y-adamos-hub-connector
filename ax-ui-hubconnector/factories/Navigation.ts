import { Injectable } from '@angular/core';
import { NavigatorNode, NavigatorNodeFactory, _ } from '@c8y/ngx-components';

@Injectable()
export class HubConnectorNavigationFactory implements NavigatorNodeFactory {

  get() {
    const nav: NavigatorNode[] = [];

     nav.push(new NavigatorNode({
      label: _('Devices'),
      icon: 'c8y-icon c8y-icon-device',
      path: '/devices',
      routerLinkExact: false,
      priority: 100
    }));
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

    var rules = new NavigatorNode({
      label: _("Event Rules"),
      icon: 'c8y-icon c8y-icon-event-processing',
      path: "/eventRules",
      routerLinkExact: true,
      priority: 99
    });

    rules.children.push(new NavigatorNode({
      label: _("Events from Hub"),
      icon: 'c8y-icon c8y-icon-events',
      path: "/eventRules/fromAdamosHub",
      routerLinkExact: true,
      priority: 99
    }));

    nav.push(rules);    

    nav.push(new NavigatorNode({
      label: _('Settings'),
      icon: 'c8y-icon c8y-icon-administration',
      path: '/settings',
      priority: 98
    }));    
    return nav;
  }
}
