//
//  InterfaceController.swift
//  PluckIt WatchKit Extension
//
//  Created by Kyle Boss on 11/15/15.
//  Copyright © 2015 Kyle Boss. All rights reserved.
//

import WatchKit
import Foundation
import WatchConnectivity


class InterfaceController: WKInterfaceController, WCSessionDelegate {

    var sharedFilePath: NSURL?
    var appFileManager:NSFileManager? = NSFileManager.defaultManager()
    var sharedContainer:NSURL?
    var session: WCSession!
    
    @IBOutlet var playButton: WKInterfaceButton!
    @IBOutlet var recordButton: WKInterfaceButton!
    
    override func awakeWithContext(context: AnyObject?) {
        super.awakeWithContext(context)
        sharedContainer = appFileManager!.containerURLForSecurityApplicationGroupIdentifier("group.pluckit")
        sharedFilePath  = sharedContainer?.URLByAppendingPathComponent("tuner.wav")
        
    }
    
    override func willActivate() {
        super.willActivate()
        
        if (WCSession.isSupported()) {
            session = WCSession.defaultSession()
            session.delegate = self
            session.activateSession()
        }
    }
    
    
    @IBAction func playAudio() {
        let options = [WKMediaPlayerControllerOptionsAutoplayKey : "true"]
        
        presentMediaPlayerControllerWithURL(sharedFilePath!, options: options,
            completion: { didPlayToEnd, endTime, error in
                if let err = error {
                    print(err.description)
                }
        })
    }

    
    @IBAction func recordAudio() {
        let duration        = NSTimeInterval(5)
        let recordOptions   = [WKAudioRecorderControllerOptionsMaximumDurationKey : duration]
        presentAudioRecorderControllerWithOutputURL(sharedFilePath!,
            preset: .NarrowBandSpeech,
            options: recordOptions,
            completion: { saved, error in
                if let err = error {
                    NSLog(err.description)
                }
                if saved {
                    self.playButton.setEnabled(true)
                    NSLog(String(self.sharedFilePath!))
                    do {
                        let attr : NSDictionary? = try NSFileManager.defaultManager().attributesOfItemAtPath(self.sharedFilePath!.path!)
                        
                        if let _attr = attr {
                            NSLog(String(_attr.fileSize()))
                        }
                    } catch {
                        print("Error: \(error)")
                    }

                    _ = WCSession.defaultSession().transferFile(self.sharedFilePath!, metadata: nil)
                }
        })
    }

    override func didDeactivate() {
        // This method is called when watch view controller is no longer visible
        super.didDeactivate()
    }

}
