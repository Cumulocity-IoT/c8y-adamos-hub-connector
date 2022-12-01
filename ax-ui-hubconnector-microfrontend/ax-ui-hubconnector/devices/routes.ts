// import { DevicesListComponent } from "./devices-list.component";
import { DevicesDetailComponent } from "./devices-detail.component";
import { NewDevicesListComponent } from "./new-devices-list.component";

/* 
 * Defines all routes for the current module 
 */
export const routes =
[
    {
        path: 'hub/devices',
        component: NewDevicesListComponent
    },
    {
        path: 'hub/devices/:id',
        component: DevicesDetailComponent
    }
];