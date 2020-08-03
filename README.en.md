# srpc

#### Description
Simple RestTemplate-based http calls.

#### Software Architecture
Software architecture description

#### Installation


#### Instructions
2. Use Mode One
```java
@RestController
@RequestMapping("test")
public class TestController{
	@Autowired
	private SimpleRpc simpleRpc;

	@PostMapping("/rpc")
	public Object rpc() {
		Request<UserEntity> build = Request.post("/auth/oauth2/check").body(new UserEntity());
		UserEntity response = simpleRpc.getForObject(build, UserEntity.class);
		System.out.println(response);
		return response;
	}
}
```

2. Use Mode Two

```java
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ServletComponentScan
@EnableSimpleRpc(basePackages = {"*******"}, mode = AdviceMode.ASPECTJ)
public class Starter {
	public static void main(String[] args) {
		SpringApplication.run(Starter.class, args);
	}
}

@RpcClient(baseUrl = "test")
public interface TestRpc {
	@Rpc(url = "test/pathtest/{id}/{name}", method = HttpMethod.POST)
	String pathtest(@PathVariable("id") String id, @PathVariable("name") String name);


	@Rpc(url = "test/paramtest", method = HttpMethod.POST)
	String paramtest(@RequestParam("id") List<String> id, @RequestParam("name") List<String> name, @RequestPart("file") Resource file);

	@Rpc(url = "test/bodytest", method = HttpMethod.POST)
	UserEntity bodytest(@RequestBody UserEntity user);
}

@RestController
@RequestMapping("test")
public class TestController{

	@Autowired
	private TestRpc testRpc;

	@PostMapping("/rpcParam")
	public Object rpcParam(@RequestParam("file") MultipartFile file) throws IOException {
		return testRpc.paramtest(Arrays.asList("特特1", "特特2"), Arrays.asList("zs", "ls"), resource);
	}
}
```
