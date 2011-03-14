/*
 * Copyright (C) 2009, Google Inc.
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>
 * and other copyright owners as documented in the project's IP log.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.eclipse.jgit.util;

import java.io.File;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FS_Win32 extends FS {
	static boolean isWin32() {
		final String osDotName = AccessController
				.doPrivileged(new PrivilegedAction<String>() {
					public String run() {
						return System.getProperty("os.name");
					}
				});
		return osDotName != null
				&& StringUtils.toLowerCase(osDotName).indexOf("windows") != -1;
	}

	public boolean supportsExecute() {
		return false;
	}

	public boolean canExecute(final File f) {
		return false;
	}

	public boolean setExecute(final File f, final boolean canExec) {
		return false;
	}

	@Override
	public boolean retryFailedLockFileCommit() {
		return true;
	}

	@Override
	protected File discoverGitPrefix() {
		String path = SystemReader.getInstance().getenv("PATH");
		File gitExe = searchPath(path, "git.exe", "git.cmd");
		if (gitExe != null)
			return gitExe.getParentFile().getParentFile();

		// This isn't likely to work, if bash is in $PATH, git should
		// also be in $PATH. But its worth trying.
		//
		String w = readPipe(userHome(), //
				new String[] { "bash", "--login", "-c", "which git" }, //
				Charset.defaultCharset().name());
		if (w != null)
			return new File(w).getParentFile().getParentFile();
		return null;
	}

	@Override
	protected File userHomeImpl() {
		String home = SystemReader.getInstance().getenv("HOME");
		if (home != null)
			return resolve(null, home);
		String homeDrive = SystemReader.getInstance().getenv("HOMEDRIVE");
		if (homeDrive != null) {
			String homePath = SystemReader.getInstance().getenv("HOMEPATH");
			return new File(homeDrive, homePath);
		}

		String homeShare = SystemReader.getInstance().getenv("HOMESHARE");
		if (homeShare != null)
			return new File(homeShare);

		return super.userHomeImpl();
	}

	@Override
	public ProcessBuilder runInShell(String cmd, String[] args) {
		List<String> argv = new ArrayList<String>(3 + args.length);
		argv.add("cmd.exe");
		argv.add("/c");
		argv.add(cmd);
		argv.addAll(Arrays.asList(args));
		ProcessBuilder proc = new ProcessBuilder();
		proc.command(argv);
		return proc;
	}
}
