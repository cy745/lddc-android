/***************************************************************************
    copyright            : (C) 2002 - 2008 by Scott Wheeler
    email                : wheeler@kde.org
 ***************************************************************************/

/***************************************************************************
 *   This library is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Lesser General Public License version   *
 *   2.1 as published by the Free Software Foundation.                     *
 *                                                                         *
 *   This library is distributed in the hope that it will be useful, but   *
 *   WITHOUT ANY WARRANTY; without even the implied warranty of            *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with this library; if not, write to the Free Software   *
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA         *
 *   02110-1301  USA                                                       *
 *                                                                         *
 *   Alternatively, this file is available under the Mozilla Public        *
 *   License Version 1.1.  You may obtain a copy of the License at         *
 *   http://www.mozilla.org/MPL/                                           *
 ***************************************************************************/

#ifndef TAGLIB_MPEGHEADER_H
#define TAGLIB_MPEGHEADER_H

#include <memory>

#include "taglib.h"
#include "taglib_export.h"

namespace TagLib {

  class ByteVector;
  class File;

  namespace MPEG {

    //! An implementation of MP3 frame headers

    /*!
     * This is an implementation of MPEG Layer III headers.  The API follows more
     * or less the binary format of these headers.  I've used
     * <a href="http://www.mp3-tech.org/programmer/frame_header.html">this</a>
     * document as a reference.
     */

    class TAGLIB_EXPORT Header
    {
    public:
      /*!
       * Parses an MPEG header based on \a file and \a offset.
       *
       * \note If \a checkLength is true, this requires the next MPEG frame to
       * check if the frame length is parsed and calculated correctly.  So it's
       * suitable for seeking for the first valid frame.
       */
      Header(File *file, offset_t offset, bool checkLength = true);

      /*!
       * Does a shallow copy of \a h.
       */
      Header(const Header &h);

      /*!
       * Destroys this Header instance.
       */
      virtual ~Header();

      /*!
       * Returns true if the frame is at least an appropriate size and has
       * legal values.
       */
      bool isValid() const;

      /*!
       * The MPEG Version.
       */
      enum Version {
        //! MPEG Version 1
        Version1 = 0,
        //! MPEG Version 2
        Version2 = 1,
        //! MPEG Version 2.5
        Version2_5 = 2,
        //! MPEG Version 4
        Version4 = 3
      };

      /*!
       * Returns the MPEG Version of the header.
       */
      Version version() const;

      /*!
       * Returns the layer version.  This will be between the values 1-3.
       */
      int layer() const;

      /*!
       * Returns true if the MPEG protection bit is enabled.
       */
      bool protectionEnabled() const;

      /*!
       * Returns the bitrate encoded in the header.
       */
      int bitrate() const;

      /*!
       * Returns the sample rate in Hz.
       */
      int sampleRate() const;

      /*!
       * Returns true if the frame is padded.
       */
      bool isPadded() const;

      /*!
       * There are a few combinations or one or two channel audio that are
       * possible:
       */
      enum ChannelMode {
        //! Stereo
        Stereo        = 0,
        //! Stereo
        JointStereo   = 1,
        //! Dual Mono
        DualChannel   = 2,
        //! Mono
        SingleChannel = 3
      };

      /*!
       * Returns the channel mode for this frame.
       */
      ChannelMode channelMode() const;

      /*!
       * MPEG-4 channel configuration.
       */
      enum ChannelConfiguration {
        Custom = 0,
        FrontCenter = 1,
        FrontLeftRight = 2,
        FrontCenterLeftRight = 3,
        FrontCenterLeftRightBackCenter = 4,
        FrontCenterLeftRightBackLeftRight = 5,
        FrontCenterLeftRightBackLeftRightLFE = 6,
        FrontCenterLeftRightSideLeftRightBackLeftRightLFE = 7
      };

      /*!
       * Returns the MPEG-4 channel configuration.
       */
      ChannelConfiguration channelConfiguration() const;

      /*!
       * Returns true if this is the header of an Audio Data Transport Stream
       * (ADTS), usually AAC.
       */
      bool isADTS() const;

      /*!
       * Returns true if the copyrighted bit is set.
       */
      bool isCopyrighted() const;

      /*!
       * Returns true if the "original" bit is set.
       */
      bool isOriginal() const;

      /*!
       * Returns the frame length in bytes.
       */
      int frameLength() const;

      /*!
       * Returns the number of frames per sample.
       */
      int samplesPerFrame() const;

      /*!
       * Makes a shallow copy of the header.
       */
      Header &operator=(const Header &h);

    private:
      void parse(File *file, offset_t offset, bool checkLength);

      class HeaderPrivate;
      TAGLIB_MSVC_SUPPRESS_WARNING_NEEDS_TO_HAVE_DLL_INTERFACE
      std::shared_ptr<HeaderPrivate> d;
    };
  }  // namespace MPEG
}  // namespace TagLib

#endif
