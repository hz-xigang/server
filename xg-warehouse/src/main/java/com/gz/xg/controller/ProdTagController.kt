package com.gz.xg.controller

import com.gz.xg.annotation.OpLog
import com.gz.xg.base.BaseController
import com.gz.xg.domain.dto.ProdTagDto
import com.gz.xg.domain.search.ProdTagSearch
import com.gz.xg.enums.BusinessType
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.ProdTagService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/productionTag")
 class ProdTagController(
    private val prodTagService: ProdTagService
)  : BaseController() {

    @OpLog(title = "标签管理", opName = "生成纸箱标签", businessType = BusinessType.INSERT)
    @PostMapping(value = [""])
    fun add(@RequestBody @Validated dto: ProdTagDto): Any {
        val pdfBytes = prodTagService.add(dto)

        // 如果生成了 PDF，返回文件下载响应
        return if (pdfBytes != null) {
            val fileName = "prodTag_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.pdf"
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes)
        } else {
            // 未配置模板时，返回普通成功响应
            success()
        }
    }

    @PostMapping(value = ["list"])
     fun list(@RequestBody search: ProdTagSearch) : ResponseResult {
        return success(prodTagService.listByProdNo(search))
    }

    @GetMapping("/tag/{tagNo}")
     fun findByTagNo(@PathVariable tagNo: String,
                     @RequestParam(value = "type", defaultValue = "0") type : Int ) : ResponseResult{
        return success( prodTagService.findVoByTagNo(tagNo,type) )
    }

    @OpLog(title = "标签管理", opName = "删除纸箱标签", businessType = BusinessType.DELETE)
    @DeleteMapping("{id}")
    fun softDelById(@PathVariable id: String) : ResponseResult{
        prodTagService.softDelById(id)
        return success()
    }

    @OpLog(title = "标签管理", opName = "补打纸箱标签", businessType = BusinessType.OTHER)
    @GetMapping("/reprint/{id}")
    fun reprint(@PathVariable id: String): Any {
        val pdfBytes = prodTagService.reprint(id)

        return if (pdfBytes != null) {
            val fileName = "prodTag_reprint_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.pdf"
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes)
        } else {
            success()
        }
    }

}