// import { DevicesListComponent } from "./devices-list.component";
import { DevicesDetailComponent } from "./devices-detail.component";
import { NewDevicesListComponent } from "./new-devices-list.component";

/* 
 * Defines all routes for the current module 
 */
export const routes =
[
    {
        path: 'devices',
        component: NewDevicesListComponent
    },
    {
        path: 'devices/:id',
        component: DevicesDetailComponent
    }
];