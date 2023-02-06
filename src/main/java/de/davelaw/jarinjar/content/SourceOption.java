package de.davelaw.jarinjar.content;

public enum SourceOption {
	/** Source will not    <i>usually</i>   be exported for this Content.<p>See.: {@link #isExported(SourceOption, SourceOption)} */        NO,
	/** Source will     <b><i>never</i></b> be exported for this Content.<p>See.: {@link #isExported(SourceOption, SourceOption)} */ INSIST_NO,
	/** Source             <i>should</i>    be exported for this Content.<p>See.: {@link #isExported(SourceOption, SourceOption)} */ INSIST_YES,
	/** Source will        <i>usually</i>   be exported for this Content.<p>See.: {@link #isExported(SourceOption, SourceOption)} */        YES;

	/**
	 * Returns whether or not Content Source should be exported.
	 * <p>
	 * If Global or Content  is {@link  #INSIST_NO},  Sources    will <b>not</b> be exported.<br>
	 * Otherwise, if Global  is {@link  #INSIST_YES}, Sources <b>will</b>        be exported.<br>
	 * Otherwise, if Content is {@link  #NO},         Sources    will <b>not</b> be exported.<br>
	 * Otherwise,                                     Sources <b>will</b>        be exported.<br>
	 * 
	 * @param  global   Option specified at Jar     level
	 * @param  content  Option specified at Content level
	 * @return
	 */
	public static boolean isExported(final SourceOption global, final SourceOption content) {

		if (global  == INSIST_NO)  {return false;}
		if (content == INSIST_NO)  {return false;}

		if (global  == INSIST_YES) {return true;}

		if (content == NO)         {return false;}
		/**/                        return true;
	}
}
