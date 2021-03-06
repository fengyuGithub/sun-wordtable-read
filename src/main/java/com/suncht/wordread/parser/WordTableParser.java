package com.suncht.wordread.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;
import com.suncht.wordread.model.WordTable;
import com.suncht.wordread.parser.mapping.IWordTableMemoryMappingVisitor;
import com.suncht.wordread.parser.strategy.ITableTransferStrategy;
import com.suncht.wordread.parser.wordh.WordHTableParser;
import com.suncht.wordread.parser.wordx.WordXTableParser;

/**
 * Word文档解析器
 * 支持2007以上的docx、2007以下的doc文档
 * @author changtan.sun
 *
 */
public class WordTableParser {
	private static final String DOCX_WORD_DOCUMENT = ".docx";
	private static final String DOC_WORD_DOCUMENT = ".doc";

	private WordTableTransferContext context;
	private IWordTableParser wordTableParser;

	private WordTableParser() {
		this.context = WordTableTransferContext.create();
	}

	public static WordTableParser create() {
		return new WordTableParser();
	}

	public WordTableParser transferStrategy(ITableTransferStrategy tableTransferStrategy) {
		context.transferStrategy(tableTransferStrategy);
		return this;
	}

	public WordTableParser memoryMappingVisitor(IWordTableMemoryMappingVisitor visitor) {
		context.visitor(visitor);
		return this;
	}

	public List<WordTable> parse(File wordFile) {
		Preconditions.checkArgument(wordFile.exists(), "文件不存在");
		
		String fileName = wordFile.getName();
		WordDocType docType = WordDocType.DOCX;
		if (StringUtils.endsWithIgnoreCase(fileName, DOCX_WORD_DOCUMENT)) {
			docType = WordDocType.DOCX;
		} else if (StringUtils.endsWithIgnoreCase(fileName, DOC_WORD_DOCUMENT)) {
			docType = WordDocType.DOC;
		} else {
			throw new IllegalArgumentException("不支持该文件类型");
		}

		try(FileInputStream inputStream = new FileInputStream(wordFile);) {
			return this.parse(inputStream, docType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public List<WordTable> parse(InputStream inputStream, WordDocType docType) {
		if (docType == WordDocType.DOCX) {
			wordTableParser = new WordXTableParser(this.context);
		} else if (docType == WordDocType.DOC) {
			wordTableParser = new WordHTableParser(this.context);
		} else {
			throw new IllegalArgumentException("不支持该文件类型");
		}
		return wordTableParser.parse(inputStream);
	}

	/**
	 * Word文档类型
	 * @author changtan.sun
	 *
	 */
	public static enum WordDocType {
		DOCX, DOC, UNKOWN
	}
}
