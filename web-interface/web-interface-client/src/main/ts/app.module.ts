import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { CreateServiceComponent }  from './create-service';
import { ServicesListComponent }  from './services-list';
import { ServiceDetailComponent }  from './service-detail';
import { UnselectedComponent }  from './unselected';

import { routing } from './app.routing';

@NgModule({
  imports:      [ BrowserModule, routing ],
  declarations: [ CreateServiceComponent, ServicesListComponent, UnselectedComponent, ServiceDetailComponent ],
  bootstrap:    [ ServicesListComponent ]
})
export class AppModule { }
