import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { IManagedObject, InventoryService, CookieAuth } from '@c8y/client';
import { BasicAuth  } from '@c8y/client';
import { IManufacturerIdentity } from './model/IManufacturerIdentity';
import { IHubResponse } from './model/IHubResponse';
import { AlertService } from '@c8y/ngx-components';
import { IEventRules } from './model/IEventRules';
import { v4 as uuid } from 'uuid';
import { IEventRule } from './model/IEventRule';
import { IEventTrigger } from './model/IEventTrigger';
import { ITreeNode } from './model/ITreeNode';

/* 
 * HubService manages the communication with the REST-endpoint of HubConnector-Microservice.
 */
@Injectable({ providedIn: 'root' })
export class AdamosHubService {
    private hubUrl = "/service/hubconnector";
    private httpOptions = {  };

    private lastDetailFilter: string;

    constructor(private http: HttpClient, private auth: BasicAuth, private cookieAuth: CookieAuth, private inventory: InventoryService, private alertService: AlertService) { 
        // Setup basic auth settings for communication with HubConnector 
        // TODO: Try HttpInterceptor instead?
        let headers = new HttpHeaders();
        // headers = headers.set('Content-Type', 'application/json').set('Authorization', 'Basic ' + this.auth.getCometdHandshake().ext['com.cumulocity.authn'].token);
        headers = headers.set('Content-Type', 'application/json')
        if (this.auth.getCometdHandshake().ext['com.cumulocity.authn'].token != undefined) {
            headers = headers.set('Authorization', 'Basic ' + this.auth.getCometdHandshake().ext['com.cumulocity.authn'].token);
        } else {
            headers = headers.set('X-XSRF-TOKEN', this.cookieAuth.getCometdHandshake().ext['com.cumulocity.authn'].xsrfToken);
        }
        this.httpOptions = { headers:  headers }
    }

    private treeDragItem: ITreeNode<any>;
    setDragItem(item: ITreeNode<any>) {
        this.treeDragItem = item;
    }

    getDragItem(): ITreeNode<any> {
        return this.treeDragItem;
    }


    setLastDetailFilter(filter: string) {
        this.lastDetailFilter = filter;
    }

    getLastDetailFilter() : string {
        return this.lastDetailFilter;
    }

    getAsset$(id: number) : Observable<IHubResponse<any>> {
        return this.getRestObject$(`/assets/${id}`, `getAsset$(${id})`);
    }

    // Adds HubSettings for an existing asset
    addAsset (id: number): Observable<IManagedObject> {
        const url = `${this.hubUrl}/synchronization/toHub/${id}`;
        return this.http.post<IManagedObject>(url, "", this.httpOptions).pipe(
            tap((asset: IManagedObject) => console.log(`added asset w/ id=${asset.id['value']}`)),
            catchError(this.handleError<IManagedObject>('addAsset'))
        );
    }      
    
    // Removes HubSettings for an existing asset
    removeAsset (id: number): Observable<{}> {
        const url = `${this.hubUrl}/synchronization/unlink/${id}`; 
        return this.http.post(url, this.httpOptions).pipe(
            catchError(this.handleError('removeAsset'))
        );
    }

    getEventRules$(direction: string): Observable<any[]> {
        const url = `/eventRules`;
        return this.getRestObject$(url, "getEventRules");
    }

    updateEventRules$(eventRules: IEventRules) {
        const url = `${this.hubUrl}/eventRules`;
        return this.http.put<any>(url, eventRules, this.httpOptions).pipe(
            tap((mapping: any) => console.log(`updated Eventrules for direction=${eventRules.direction}`)),
            catchError(this.handleError<any>('updateEventRules'))
        );
    }

    deleteMapping (id: number): Observable<{}> {
        const url = `${this.hubUrl}/mappings/${id}`;
        return this.http.delete(url, this.httpOptions).pipe(
            catchError(this.handleError('deleteMapping'))
        );
    }

    getTree$(): Observable<ITreeNode<any>[]> {
        return this.getRestObject$("/hierarchy/tree", "getTree");
    }

    // Promise-based usage of InventoryService.
    async getMappings() {
        let filterMappings: object = {
            //fragmentType: 'c8y_IsDevice',
            query: "$filter=has('com_adamos_hubconnector_model_MappingConfiguration') $orderby=id asc",
            // paging information will be a part of the response now
            withTotalPages: true,
            pageSize: 100
        };

        const { data, res, paging } = await this.inventory.list(filterMappings);
        return data;
    }

    getMappings$(): Observable<IHubResponse<any>[]> {
        return this.getRestObject$("/mappings", "getMappings");
    }

    getGlobalSettings$(): Observable<any> {
        return this.getRestObject$("/globalSettings", "getGlobalSettings");
    }

    updateGlobalSettings$(globalSettings: any) {
        const url = `${this.hubUrl}/globalSettings`;
        return this.http.put<any>(url, globalSettings, this.httpOptions).pipe(
            tap((settings: any) => console.log(`saveGlobalSettings`)),
            catchError(this.handleError<any>('saveGlobalSettings'))
        );
    }

    addMapping$() {
        const url = `${this.hubUrl}/mappings`; // POST service/hubconnector/mappings
        return this.http.post<any>(url, "", this.httpOptions).pipe(
            tap((mapping: any) => console.log(`added mapping w/ id=${mapping.id['value']}`)),
            catchError(this.handleError<any>('addMapping'))
        );
    }

    duplicateMapping$(id: number) {
        const url = `${this.hubUrl}/mappings/${id}/duplicate`; // POST service/hubconnector/mappings/42/duplicate
        return this.http.post<any>(url, "", this.httpOptions).pipe(
            tap((mapping: any) => console.log(`duplicated mapping w/ id=${id} to new id=${mapping.id['value']}`)),
            catchError(this.handleError<any>('duplicateMapping'))
        );
    }    

    private getRestObject$<T>(path: string, errorTitle: string): Observable<T> {
        return this.http.get<T>(`${this.hubUrl}${path}`, this.httpOptions).pipe(
            catchError(this.handleError<T>(errorTitle))
        );
    }

    static equalObjects<T>(o1: T, o2: T): boolean {
        return JSON.stringify(o1) === JSON.stringify(o2);
    }

    static cloneObject<T>(objectToCopy: T): T{
        return (JSON.parse(JSON.stringify(objectToCopy)));
    }

    static duplicateEventRule(rule: IEventRule): IEventRule {
        var newRule = AdamosHubService.cloneObject(rule);
        newRule.id = uuid();
        newRule.name = newRule.name + " (Copy)";
        return newRule;
    }    

    static createEventRule(): IEventRule {
        var response = {} as IEventRule;
        response.id = uuid();
        response.name = "";
        response.enabled = true;
        response.eventTrigger = {} as IEventTrigger;
        response.eventTrigger["@type"] =  "HubEventTrigger";

        response.eventProcessor = {};
        response.eventProcessor["@type"] = "AdamosEventProcessor";
        response.output = "";

        // response.attributeProcessingMode = "ALL";
        //response.adamos = {} as IAdamosRule;
        response.eventProcessor.channel = "EVENTS";
        response.eventProcessor.processingMode = "PERSISTENT";
        return response;
    }

    getManufacturerIdentities$(): Observable<Array<IManufacturerIdentity>> {
        return this.getRestObject$("/manufacturerIdentities", "getManufacturerIdentities");
    }

    getEquipment$(): Observable<Array<any>> {
        return this.getRestObject$("/assets", "getEquipment");
    }

    getDisconnectedEquipment$(): Observable<Array<any>> {
        return this.getRestObject$("/assets?disconnected=true", "getDisconnectedEquipment");
    }

    importHubDevice$(uuid: number, isDevice: boolean) {
        const url = `${this.hubUrl}/synchronization/fromHub/${uuid}?isDevice=${isDevice}`;
        return this.http.post<any>(url, "", this.httpOptions).pipe(
            tap((device: any) => console.log(`imported device w/ uuid=${uuid} to new id=${device.id['value']}`)),
            catchError(this.handleError<any>('importHubDevice'))
        );
    }   

    getMapping$(id: number) : Observable<any> {
        return this.getRestObject$(`/mappings/${id}`, `getMapping$(${id})`);
    }

    updateMapping$(data: any) {
        const url = `${this.hubUrl}/mappings`; 
        return this.http.put<any>(url, data, this.httpOptions).pipe(
            tap((mapping: any) => console.log(`updated mapping w/ id=${data.id}`)),
            catchError(this.handleError<any>('updateMapping'))
        );
    } 
    
    // Generic error-handler
    private handleError<T> (operation = 'operation', result?: T) {
        return (error: any): Observable<T> => {
        
            // TODO: send the error to remote logging infrastructure
            console.error(error); // log to console instead

            this.alertService.addByText("danger", error.message, error);
        
            // TODO: better job of transforming error for user consumption
            //this.log(`${operation} failed: ${error.message}`);
        
            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }

}