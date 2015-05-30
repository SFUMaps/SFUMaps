//
//  ViewController.swift
//  GMapsDemo
//
//  Created by Bosco Noronha on 29/3/15.
//  Copyright (c) 2015 boss co. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var viewMap: GMSMapView!
    
    @IBOutlet weak var bbFindAddress: UIBarButtonItem!
    
    @IBOutlet weak var lblInfo: UILabel!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        //SFU AQ location
        let camera: GMSCameraPosition = GMSCameraPosition.cameraWithLatitude(
            49.279014, longitude: -122.916528, zoom: 18.0)
        viewMap.camera = camera
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


    // MARK: IBAction method implementation
    
    @IBAction func changeMapType(sender: AnyObject) {
        
        let actionSheet = UIAlertController(title: "Map Types", message: "Select map type:", preferredStyle: UIAlertControllerStyle.ActionSheet)
        
        let normalMapTypeAction = UIAlertAction(title: "Normal", style: UIAlertActionStyle.Default) { (alertAction) -> Void in
            self.viewMap.mapType = kGMSTypeNormal
        }
        
        let terrainMapTypeAction = UIAlertAction(title: "Terrain", style: UIAlertActionStyle.Default) { (alertAction) -> Void in
            self.viewMap.mapType = kGMSTypeTerrain
        }
        
        let hybridMapTypeAction = UIAlertAction(title: "Hybrid", style: UIAlertActionStyle.Default) { (alertAction) -> Void in
            self.viewMap.mapType = kGMSTypeHybrid
        }
        
        let cancelAction = UIAlertAction(title: "Close", style: UIAlertActionStyle.Cancel) { (alertAction) -> Void in
            
        }
        
        actionSheet.addAction(normalMapTypeAction)
        actionSheet.addAction(terrainMapTypeAction)
        actionSheet.addAction(hybridMapTypeAction)
        actionSheet.addAction(cancelAction)
        
        presentViewController(actionSheet, animated: true, completion: nil)
        
    }
    
    
    @IBAction func findAddress(sender: AnyObject) {
    
    }
    
    
    @IBAction func createRoute(sender: AnyObject) {
    
    }
    
    
    @IBAction func changeTravelMode(sender: AnyObject) {
    
    }
    
    
}

