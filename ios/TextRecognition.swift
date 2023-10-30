import MLKitTextRecognition
import MLKitVision

extension String {
  func stripPrefix(_ prefix: String) -> String {
    guard hasPrefix(prefix) else { return self }
    return String(dropFirst(prefix.count))
  }
}

@objc(TextRecognition)
class TextRecognition: NSObject {
  @objc(recognizeText:withResolver:withRejecter:)
  func recognizeText(imgPath: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard !imgPath.isEmpty else { reject("ERR", "You must include the image path", nil); return }

    let formattedImgPath = imgPath.stripPrefix("file://")

    do {
      let imgData = try Data(contentsOf: URL(fileURLWithPath: formattedImgPath))
      let image = UIImage(data: imgData)!

      let visionImage = VisionImage(image: image)
      visionImage.orientation = image.imageOrientation

      let textRecognizer = TextRecognizer.textRecognizer()

      textRecognizer.process(visionImage) { result, error in
        guard error == nil, let result = result else { return }

        // debug
        let resultText = result.text
        print("===========================")
        print(resultText)
        print("===========================")

        var recognizedTextBlocks = [String]()

        for block in result.blocks {
          recognizedTextBlocks.append(block.text)
        }

        resolve(recognizedTextBlocks)
      }
    } catch {
      print(error)
      reject("ERR", error.localizedDescription, nil)
    }
  }
  @objc(recognize:withResolver:withRejecter:)
  func recognize(imgPath: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    guard !imgPath.isEmpty else { reject("ERR", "You must include the image path", nil); return }
    let formattedImgPath = imgPath.stripPrefix("file://")

    do {
      let imgData = try Data(contentsOf: URL(fileURLWithPath: formattedImgPath))
      let image = UIImage(data: imgData)!

      let visionImage = VisionImage(image: image)
      visionImage.orientation = image.imageOrientation

      let textRecognizer = TextRecognizer.textRecognizer()

      textRecognizer.process(visionImage) { [self] result, error in
        guard error == nil, let result = result else { return }
        let resultText = result.text
        print("===========================")
        print(resultText)
        print("===========================")
    
        let output = self.prepareOutput(result: result)
        resolve(output)
      }
    } catch {
        print(error)
        reject("ERR", error.localizedDescription, nil)
    }
  }
  
  func prepareOutput(result: Text) -> [[String: Any]] {
    var output = [[String: Any]]()
    for block in result.blocks {
      var blockElements = [[String: Any]]()
      for line in block.lines {
        var lineElements = [[String: Any]]()
        for element in line.elements {
          var e = [String: Any]()
          e["text_element"] = element.text
          lineElements.append(e)
        }
        var l = [String: Any]()
        l["text_line"] = line.text
        l["elements"] = lineElements

        blockElements.append(l)
      }
      var b = [String: Any]()
      b["text_block"] = block.text
      b["lines"] = blockElements
      output.append(b)
    }
    return output
  }
}
