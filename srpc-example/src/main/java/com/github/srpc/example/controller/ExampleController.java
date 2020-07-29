package com.github.srpc.example.controller;

import com.github.srpc.core.rpc.SimpleRpc;
import com.github.srpc.core.rpc.request.Request;
import com.github.srpc.example.rpc.TestRpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: phz
 * @create: 2020-07-29 11:19:56
 */
@RestController
@RequestMapping("/example")
public class ExampleController {

	@Autowired
	private SimpleRpc simpleRpc;

	@Autowired
	private TestRpc testRpc;


	@PostMapping("/test1")
	public ResponseEntity<Object> test1() {
		Request<Object> build = Request.post("test/example/pathTest/{id}/{name}").uriParam("id", "10101").uriParam("name", "zs").build();
		String res = simpleRpc.getForObject(build, String.class);
		System.out.println(res);

		testRpc.bodyTest(Collections.singletonMap("name", "zs")).thenAccept(System.out::println);
		return ResponseEntity.ok("ok");
	}

	@PostMapping("/test2")
	public ResponseEntity<Object> test2() {
		ByteArrayResource byteArrayResource = new ByteArrayResource("jljkljlkjlk".getBytes()) {
			@Override
			public String getFilename() {
				return "测试txt";
			}
		};
		Request<Object> build = Request.post("test/example/paramTest")
				.formParam("id", "10101")
				.formParam("name", "zs")
				.formParam("file", byteArrayResource)
				.build();

		List<String> forList = simpleRpc.getForList(build, String.class);
		return ResponseEntity.ok(forList);
	}


	@PostMapping("/pathTest/{id}/{name}")
	public ResponseEntity<Object> pathTest(@PathVariable("id") String id, @PathVariable("name") String name) {
		System.out.println("id:" + id);
		System.out.println("name:" + name);
		return ResponseEntity.ok(name);
	}

	@PostMapping("/paramTest")
	public ResponseEntity<Object> paramTest(@RequestParam("id") List<String> id, @RequestParam("name") List<String> name, @RequestPart("file") MultipartFile file) {
		System.out.println(id);
		System.out.println(name);
		System.out.println(file.getOriginalFilename());
		return ResponseEntity.ok(name);
	}

	@PostMapping("/bodyTest")
	public ResponseEntity<Object> bodyTest(@RequestBody Map<String, String> param) {
		System.out.println(param);
		return ResponseEntity.ok(Collections.singletonList("sdsdd"));
	}
}
