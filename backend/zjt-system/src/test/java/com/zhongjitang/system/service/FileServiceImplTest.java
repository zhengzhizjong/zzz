package com.zhongjitang.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhongjitang.common.core.exception.BusinessException;
import com.zhongjitang.common.core.exception.ErrorCode;
import com.zhongjitang.common.core.result.R;
import com.zhongjitang.system.domain.entity.SysFileDO;
import com.zhongjitang.system.mapper.SysFileMapper;
import com.zhongjitang.system.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private SysFileMapper fileMapper;

    @InjectMocks
    private FileServiceImpl fileService;

    @TempDir
    Path tempDir;

    private SysFileDO mockFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(fileService, "baseUrl", "http://localhost:8080/files");

        mockFile = new SysFileDO();
        mockFile.setId(1L);
        mockFile.setFileName("test.txt");
        mockFile.setOriginalName("test.txt");
        mockFile.setFilePath("2026/06/05/test.txt");
        mockFile.setFileSize(12L);
        mockFile.setFileType("document");
        mockFile.setMimeType("text/plain");
        mockFile.setBucketName("local");
        mockFile.setUrl("http://localhost:8080/files/2026/06/05/test.txt");
        mockFile.setBusinessType("member");
        mockFile.setBusinessId("1");
    }

    @Test
    void testUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "hello world".getBytes());

        when(fileMapper.insert(any(SysFileDO.class))).thenReturn(1);

        R<SysFileDO> result = fileService.upload(file, "member", "1");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals("test.txt", result.getData().getOriginalName());
        assertEquals("document", result.getData().getFileType());
        assertEquals("member", result.getData().getBusinessType());
        verify(fileMapper, times(1)).insert(any(SysFileDO.class));
    }

    @Test
    void testUploadEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", new byte[0]);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileService.upload(file, null, null);
        });
        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    void testGetById() {
        when(fileMapper.selectById(1L)).thenReturn(mockFile);

        R<SysFileDO> result = fileService.getById(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("test.txt", result.getData().getOriginalName());
    }

    @Test
    void testGetByIdNotFound() {
        when(fileMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileService.getById(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testDelete() {
        when(fileMapper.selectById(1L)).thenReturn(mockFile);
        when(fileMapper.deleteById(1L)).thenReturn(1);

        R<Void> result = fileService.delete(1L);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        verify(fileMapper, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNotFound() {
        when(fileMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fileService.delete(999L);
        });
        assertEquals(ErrorCode.NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void testGetByBusiness() {
        when(fileMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(mockFile));

        R<List<SysFileDO>> result = fileService.getByBusiness("member", "1");

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void testUploadImageType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "fake-image".getBytes());

        when(fileMapper.insert(any(SysFileDO.class))).thenReturn(1);

        R<SysFileDO> result = fileService.upload(file, null, null);

        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("image", result.getData().getFileType());
    }
}
