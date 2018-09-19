package com.storage.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.storage.entity.Productimg;
import com.storage.entity.custom.SeaWeedFileSystem;
import com.storage.entity.custom.StorageResult;
import com.storage.service.ProductimgService;
import com.storage.utils.JsonUtils;

@RestController()
@PropertySource("classpath:myapp.properties")
@RequestMapping("/productimg")
public class ProductimgController   {

	@Autowired
	ProductimgService service;
	@Value("${seaweeddfs.url}")
	String seaweeddfsUrl;
	/* (non-Javadoc)
	 * @see com.storage.controller.IProductimgController#addProductimg(com.storage.entity.Productimg)
	 */
	
	@PostMapping("/add")
	public Object addProductimg(@RequestParam(value="file",required=false) MultipartFile file) {

		if(file==null ||file.isEmpty())
			return StorageResult.failed("not img present");
		String dir = addImg(file);
		if(dir.startsWith("error"))
			return StorageResult.failed(dir);
		Productimg img=new Productimg();
		img.setUrl(dir);
		return this.service.addProductimg(img);
	}
	
	@PostMapping("/addWithFullURL")
	public Object addWithFullURL(@RequestParam(value="file",required=false) MultipartFile file) {
		
		if(file==null ||file.isEmpty())
			return StorageResult.failed("not img present");
		String dir = addImg(file);
		if(dir.startsWith("error"))
			return StorageResult.failed(dir);
		Productimg img=new Productimg();
		img.setUrl(dir);
		StorageResult<Productimg> addProductimg = this.service.addProductimg(img);
		Productimg result = addProductimg.getResult();
		if(result!=null) {
			result.setUrl(seaweeddfsUrl+":8080/"+ result.getUrl());
		}
		return addProductimg;
	}
	
	private String addImg(MultipartFile file) {
		
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder().url(seaweeddfsUrl+":9333/dir/assign").get().build();
		try {
			//request server to get fid
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				 String body = response.body().string();
				 response.body().close();
				 SeaWeedFileSystem jsonToPojo = JsonUtils.jsonToPojo(body, SeaWeedFileSystem.class);
				 String fid = jsonToPojo.getFid();
				 //get suffix of the orignal file
				 String originalFilename = file.getOriginalFilename();
				 int lastIndexOf = originalFilename.lastIndexOf('.');
				 
				 String suffix = originalFilename.substring(lastIndexOf,originalFilename.length());
				 
				 String fileUrl= seaweeddfsUrl+":8080/"+fid;
				 //file body
				 com.squareup.okhttp.RequestBody fileBody = com.squareup.okhttp.RequestBody.create(MediaType.parse("image/png"), file.getBytes());
				 //file body with name and file name on 
				 com.squareup.okhttp.RequestBody bodyWithPicture = new MultipartBuilder().
						 addPart(Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"" + fid+suffix + "\""),
								 fileBody).build();
				 //request
				  request = new Request.Builder().url(fileUrl).post(bodyWithPicture).build();
				  //execute request
				  Response responseFile = okHttpClient.newCall(request).execute();
				 if(responseFile.isSuccessful())
				 {					  					 
					 return fid+suffix;
				 }
				 else {
					 return "error: when inserting img into img server";
				 }
			}else {
				String string = response.body().string();
				response.body().close();
				
				return "error:"+string;	
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "error: okHttpClient";
		}
		
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IProductimgController#getProductimg(java.lang.Integer)
	 */
	
	@GetMapping("/get/{id}")
	public Object getProductimg(@PathVariable(name = "id") Integer id) {

		return this.service.getProductimgById(id);
	}

	/* (non-Javadoc)
	 * @see com.storage.controller.IProductimgController#deleteProductimgById(java.lang.Integer)
	 */
	
	@GetMapping("/delete/{id}")
	public Object deleteProductimgById(@PathVariable(name = "id") Integer id) {
		return this.service.deleteProductimgById(id);
	}



	/* (non-Javadoc)
	 * @see com.storage.controller.IProductimgController#updateProductimg(com.storage.entity.Productimg)
	 */
	
	@PostMapping("/update")
	public Object updateProductimg(Productimg productimg) {
		return this.service.updateProductimg(productimg);
	}
	/* (non-Javadoc)
	 * @see com.storage.controller.IProductimgController#count()
	 */
	
	@GetMapping("/count")
	public Object count() {
		return this.service.count();
	}


}
