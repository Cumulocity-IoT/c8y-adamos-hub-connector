import { Injectable } from "@angular/core";
import { FetchClient, IManagedObject } from "@c8y/client";
import {
  AdamosMappingResponse,
  IEventMapping,
} from "ax-ui-hubconnector/eventRules/event-rules-to-hub.component";
import { isArray } from "lodash-es";
import { AdamosHubDevice } from "./model/AdamosDevice";

@Injectable({ providedIn: "root" })
export class NewAdamosHubService {
  private hubUrl = "/service/hubconnector";
  private headers: object = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };

  constructor(private fc: FetchClient) {}

  async getEquipment(params?: object): Promise<AdamosHubDevice[]> {
    const url = `${this.hubUrl}/assets`;

    const response = await this.fc.fetch(url, {
      method: "GET",
      headers: this.headers,
      params,
    });

    if (response.status !== 200) {
      const error = new Error();
      error.message = `Status not ok (${response.status})`;
      throw error;
    }

    const data = await response.json();
    if (isArray(data)) {
      return data as AdamosHubDevice[];
    } else {
      return [];
    }
  }

  async getDisconnectedEquipment(): Promise<AdamosHubDevice[]> {
    return this.getEquipment({ disconnected: true });
  }

  async linkHubDevice(uuid: string, isDevice: boolean = true): Promise<any> {
    const url = `${this.hubUrl}/synchronization/fromHub/${uuid}?isDevice=${isDevice}`;
    const response = await this.fc.fetch(url, {
      method: "POST",
      headers: this.headers,
    });

    if (response.status !== 201) {
      const error = new Error();
      error.message = `Status not ok (${response.status})`;
      throw error;
    }

    const data = await response.json();
    return data;
  }

  async linkCumulocityDevice(id: string): Promise<IManagedObject> {
    const url = `${this.hubUrl}/synchronization/toHub/${id}`;
    const response = await this.fc.fetch(url, {
      method: "POST",
      headers: this.headers,
    });

    if (response.status !== 201) {
      const error = new Error();
      error.message = `Status not ok (${response.status})`;
      throw error;
    }

    const data = (await response.json()) as IManagedObject;
    return data;
  }

  async unlink(id: string) {
    const url = `${this.hubUrl}/synchronization/unlink/${id}`;
    const response = await this.fc.fetch(url, {
      method: "POST",
      headers: this.headers,
    });

    if (response.status !== 204) {
      const error = new Error();
      error.message = `Status not ok (${response.status})`;
      throw error;
    }
  }

  async linkExisting(uuid: string, moId: string) {
    const url = `${this.hubUrl}/synchronization/sync/${uuid}/with/${moId}`;
    const response = await this.fc.fetch(url, {
      method: "POST",
      headers: this.headers,
    });

    if (response.status !== 200) {
      const error = new Error();
      error.message = `Status not ok (${response.status})`;
      throw error;
    }

    const data = await response.json();
    return data;
  }

  mappings: IEventMapping[] = [
    {
      c8yEventType: "c8y_Position",
      c8yFragments: ["foo", "bar"],
      c8yDevices: ["2318011", "2318012", "44297003"],
      enabled: false,
      name: "Test",
      id: "d982b35d-b932-432c-8cda-a604daac9137",
      adamosEventType: "adamos:runstate:event:resource:stackLight:*:update:1",
    },
    {
      c8yEventType: "c8y_Whatever",
      c8yFragments: ["hans", "wurst"],
      c8yDevices: ["2318011", "2318012", "44297003"],
      enabled: false,
      name: "Test2",
      id: "d982b35d-b932-432c-8cda-a604daac9138",
      adamosEventType: "adamos:runstate:event:resource:stackLight:*:update:2",
    },
  ];

  async getMappingRules(): Promise<AdamosMappingResponse> {
    return this.delay(2000).then(() =>
      Promise.resolve({
        direction: "TO_HUB",
        rules: this.mappings
      })
    );
  }

  private delay(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  updateMappingRules(response: AdamosMappingResponse): Promise<void> {
    this.mappings = response.rules;
    return this.delay(2000);
  }
}
