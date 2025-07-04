/**************************************************************************
    copyright            : (C) 2009 by Lukáš Lalinský
    email                : lalinsky@gmail.com
 **************************************************************************/

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

#ifndef TAGLIB_MP4COVERART_H
#define TAGLIB_MP4COVERART_H

#include "tlist.h"
#include "tbytevector.h"
#include "taglib_export.h"
#include "mp4atom.h"

namespace TagLib {
  namespace MP4 {
    class TAGLIB_EXPORT CoverArt
    {
    public:
      /*!
       * This describes the image type.
       */
      enum Format {
        JPEG    = TypeJPEG,
        PNG     = TypePNG,
        BMP     = TypeBMP,
        GIF     = TypeGIF,
        Unknown = TypeImplicit,
      };

      CoverArt(Format format, const ByteVector &data);
      virtual ~CoverArt();

      CoverArt(const CoverArt &item);

      /*!
       * Copies the contents of \a item into this CoverArt.
       */
      CoverArt &operator=(const CoverArt &item);

      /*!
       * Exchanges the content of the CoverArt by the content of \a item.
       */
      void swap(CoverArt &item) noexcept;

      //! Format of the image
      Format format() const;

      //! The image data
      ByteVector data() const;

    private:
      class CoverArtPrivate;
      TAGLIB_MSVC_SUPPRESS_WARNING_NEEDS_TO_HAVE_DLL_INTERFACE
      std::shared_ptr<CoverArtPrivate> d;
    };

    using CoverArtList = List<CoverArt>;
  }  // namespace MP4
}  // namespace TagLib
#endif
