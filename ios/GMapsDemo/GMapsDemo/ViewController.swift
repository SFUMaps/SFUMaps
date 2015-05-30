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
        
    }
    
    
    @IBAction func findAddress(sender: AnyObject) {
    
    }
    
    
    @IBAction func createRoute(sender: AnyObject) {
    
    }
    
    
    @IBAction func changeTravelMode(sender: AnyObject) {
    
    }
    
    
}

