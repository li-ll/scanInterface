# scanInterface
介绍：按照指定的本地目录进行restful接口扫描

原理：根据正则先匹配@RequestMapping后面的内容然后再匹配PostMapping|PutMapping|DeleteMapping|GetMapping后面的内容

> 正则1

```js
@RequestMapping\("([^"]+)"//这个匹配RequestMapping后面括号里面的内容
```

> 正则2

```js
@(PostMapping|PutMapping|DeleteMapping|GetMapping)\\(\"([^\"]*)\"\\)"//这个匹配这四个Mapping后面括号里面的内容
```

例子：

url

> http://localhost:8080/scan/filepath

入参

```json

{
    "path" : "E:\\desktop\\ruoyi-vue-pro-master\\yudao-module-system\\yudao-module-system-biz\\src\\main\\java\\cn\\iocoder\\yudao\\module\\system\\controller\\admin\\dept"
}
```

结果

```json
{
    "code": 200,
    "message": "成功扫描到数据",
    "data": [
        "post /system/dept/create",
        "put /system/dept/update",
        "delete /system/dept/delete",
        "get /system/dept/list",
        "get /system/dept/list-all-simple",
        "get /system/dept/get",
        "post /system/post/create",
        "put /system/post/update",
        "delete /system/post/delete",
        "get /system/post/list-all-simple",
        "get /system/post/page",
        "get /system/post/export"
    ]
}
```



注意现只支持注解括号里面的默认值，如下面的@PostMapping("create")就可以识别出来，暂不支持其他形式的写法如（@PostMapping(value="create")）之类的

```Java
@Tag(name = "管理后台 - 部门")
@RestController
@RequestMapping("/system/dept")
@Validated
public class DeptController {

    @Resource
    private DeptService deptService;

    @PostMapping("create")
    @Operation(summary = "创建部门")
    @PreAuthorize("@ss.hasPermission('system:dept:create')")
    public CommonResult<Long> createDept(@Valid @RequestBody DeptCreateReqVO reqVO) {
        Long deptId = deptService.createDept(reqVO);
        return success(deptId);
    }

    @PutMapping("update")
    @Operation(summary = "更新部门")
    @PreAuthorize("@ss.hasPermission('system:dept:update')")
    public CommonResult<Boolean> updateDept(@Valid @RequestBody DeptUpdateReqVO reqVO) {
        deptService.updateDept(reqVO);
        return success(true);
    }

    @DeleteMapping("delete")
    @Operation(summary = "删除部门")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dept:delete')")
    public CommonResult<Boolean> deleteDept(@RequestParam("id") Long id) {
        deptService.deleteDept(id);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取部门列表")
    @PreAuthorize("@ss.hasPermission('system:dept:query')")
    public CommonResult<List<DeptRespVO>> getDeptList(DeptListReqVO reqVO) {
        List<DeptDO> list = deptService.getDeptList(reqVO);
        list.sort(Comparator.comparing(DeptDO::getSort));
        return success(DeptConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取部门精简信息列表", description = "只包含被开启的部门，主要用于前端的下拉选项")
    public CommonResult<List<DeptSimpleRespVO>> getSimpleDeptList() {
        // 获得部门列表，只要开启状态的
        DeptListReqVO reqVO = new DeptListReqVO();
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<DeptDO> list = deptService.getDeptList(reqVO);
        // 排序后，返回给前端
        list.sort(Comparator.comparing(DeptDO::getSort));
        return success(DeptConvert.INSTANCE.convertList02(list));
    }

    @GetMapping("/get")
    @Operation(summary = "获得部门信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:dept:query')")
    public CommonResult<DeptRespVO> getDept(@RequestParam("id") Long id) {
        return success(DeptConvert.INSTANCE.convert(deptService.getDept(id)));
    }

}

```



问题小结

一开始没有把文件一次性全部读进来导致识别有问题，因为这个逻辑就是根据一整个文件从上到下的一个顺序来写的

```Java
public class readSpecificFile {
    public static void main(String[] args) {
        String directoryPath =  "E:\\desktop\\pro\\src\\main\\java\\org\\example\\controller"; // 指定目录的路径

        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            System.err.println("指定路径不是一个目录");
            return;
        }

        File[] files = directory.listFiles(); // 获取目录中的所有文件
        if (files == null || files.length == 0) {
            System.out.println("目录中没有文件");
            return;
        }
        List<String> list=new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                 System.out.println(file.getName()); // 输出文件名
                // 在这里可以使用FileInputStream等类读取文件内容
                Pattern requestMappingPattern = Pattern.compile("@RequestMapping\\(\"([^\"]+)\""); // 匹配@RequestMapping注解的正则表达式模式
                Pattern mappingPattern = Pattern.compile("@(PostMapping|PutMapping|DeleteMapping|GetMapping)\\(\"([^\"]*)\"\\)");

                // 正则表达式匹配模式
                try {
                    //这一块读取文件
                    FileInputStream inputStream = new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
/**改成了下面这种方式
 *   try (BufferedReader br = new BufferedReader(new FileReader(file))) {
 *                     StringBuilder contentBuilder = new StringBuilder();
 *                     String line;
 *                     while ((line = br.readLine()) != null) {
 *                         contentBuilder.append(line).append(System.lineSeparator());
 *                     }
 */
                 
                        
                        String content = new String(buffer, 0, len, "UTF-8");
                        Matcher requestMappingMatcher = requestMappingPattern.matcher(content); // 匹配@RequestMapping注解
                        String requestParameter="";
                        while (requestMappingMatcher.find()) {
                          requestParameter = requestMappingMatcher.group(1); // 获取注解参数
                        }
                        Matcher mappingMatcher = mappingPattern.matcher(content);
                        while (mappingMatcher.find()) {
                           //加入集合。。。
                            }
                        }
                    }
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        for (String s:list) {
            System.out.println(s);
        }
    }
}
```

