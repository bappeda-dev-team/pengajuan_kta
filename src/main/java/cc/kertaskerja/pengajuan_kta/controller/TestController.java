package cc.kertaskerja.pengajuan_kta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test")
public class TestController {

    @PostMapping("/simple")
    public ResponseEntity<String> simpleTest(@RequestParam("name") String name) {
        return ResponseEntity.ok("Received: " + name);
    }

    @PostMapping("/multipart")
    public ResponseEntity<String> multipartTest(
            @RequestParam("name") String name,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        String response = "Name: " + name;
        if (file != null) {
            response += ", File: " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)";
        }
        return ResponseEntity.ok(response);
    }
}