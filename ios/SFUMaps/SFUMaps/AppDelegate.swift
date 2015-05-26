//
//  AppDelegate.swift
//  SFUMaps
//
//  Created by Raja Bosco Noronha on 1/4/15.
//  Copyright (c) 2015 Raja Noronha. All rights reserved.
//

import UIKit


@UIApplicationMain

class AppDelegate: UIResponder, UIApplicationDelegate {
  
  var window: UIWindow?
  // 1
  let googleMapsApiKey = "AIzaSyAESyQ3QOxfiLHfNViGoQx5S2XzIg5juCg"
  
  func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {
    //2
    GMSServices.provideAPIKey(googleMapsApiKey)
    return true
  }
}